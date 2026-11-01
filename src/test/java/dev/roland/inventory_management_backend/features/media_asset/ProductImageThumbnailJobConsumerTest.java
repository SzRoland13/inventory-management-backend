package dev.roland.inventory_management_backend.features.media_asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.roland.inventory_management_backend.common.async.AsyncJobEnvelope;
import dev.roland.inventory_management_backend.common.async.AsyncJobType;
import dev.roland.inventory_management_backend.common.async.OutboxEncryptionService;
import dev.roland.inventory_management_backend.common.async.OutboxMessage;
import dev.roland.inventory_management_backend.common.async.OutboxMessageRepository;
import dev.roland.inventory_management_backend.common.async.OutboxStatus;
import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_asset.service.ProductThumbnailService;

@ExtendWith(MockitoExtension.class)
class ProductImageThumbnailJobConsumerTest {
  @Mock private OutboxMessageRepository outboxRepository;
  @Mock private OutboxEncryptionService encryptionService;
  @Mock private MediaAssetService mediaAssetService;
  @Mock private ObjectStorageService objectStorage;
  @Mock private AppConfiguration appConfiguration;
  @Mock private ProductThumbnailService productThumbnailService;

  private ProductImageThumbnailJobConsumer consumer;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    consumer =
        new ProductImageThumbnailJobConsumer(
            outboxRepository,
            encryptionService,
            new ObjectMapper(),
            mediaAssetService,
            objectStorage,
            new dev.roland.inventory_management_backend.features.media_asset.service
                .ThumbnailImageProcessor(),
            appConfiguration,
            productThumbnailService);
  }

  @Test
  void generatesAndStoresTheDeterministicWebpThumbnail() throws Exception {
    final UUID id = UUID.randomUUID();
    final byte[] payload = {1, 2, 3};
    final var envelope =
        new AsyncJobEnvelope(id, AsyncJobType.PRODUCT_IMAGE_THUMBNAIL, payload, 9L, null);
    final var message =
        new OutboxMessage(id, AsyncJobType.PRODUCT_IMAGE_THUMBNAIL, payload, 9L, null);
    final var asset =
        MediaAsset.builder()
            .id(14L)
            .objectPath("temp/original.png")
            .thumbnailObjectPath("thumbnails/14.webp")
            .thumbnailStatus(MediaThumbnailStatus.PENDING)
            .build();
    final ByteArrayOutputStream image = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(300, 100, BufferedImage.TYPE_INT_RGB), "png", image);

    when(outboxRepository.lockById(id)).thenReturn(Optional.of(message));
    when(encryptionService.decrypt(payload)).thenReturn("{\"mediaAssetId\":14}".getBytes());
    when(mediaAssetService.findByIdOrThrow(14L)).thenReturn(asset);
    when(productThumbnailService.objectPath(14L)).thenReturn("thumbnails/14.webp");
    when(objectStorage.download("temp/original.png")).thenReturn(image.toByteArray());
    when(mediaAssetService.save(asset)).thenReturn(asset);

    consumer.consume(envelope);

    assertEquals(MediaThumbnailStatus.READY, asset.getThumbnailStatus());
    assertEquals("thumbnails/14.webp", asset.getThumbnailObjectPath());
    assertEquals(OutboxStatus.SENT, message.getStatus());
    verify(objectStorage)
        .upload(eq("thumbnails/14.webp"), any(Long.class), eq("image/webp"), any());
  }

  @Test
  void deadLettersAJobWithAnInvalidAssetIdentifier() {
    final UUID id = UUID.randomUUID();
    final byte[] encrypted = {5};
    final var envelope =
        new AsyncJobEnvelope(id, AsyncJobType.PRODUCT_IMAGE_THUMBNAIL, encrypted, 9L, null);
    final var message =
        new OutboxMessage(id, AsyncJobType.PRODUCT_IMAGE_THUMBNAIL, encrypted, 9L, null);
    when(outboxRepository.lockById(id)).thenReturn(Optional.of(message));
    when(encryptionService.decrypt(encrypted)).thenReturn("{\"mediaAssetId\":0}".getBytes());

    consumer.consume(envelope);

    assertEquals(OutboxStatus.DLQ_PENDING, message.getStatus());
    assertEquals(1, message.getDeliveryAttempts());
    assertNull(message.getLeaseToken());
  }

  @Test
  void schedulesRetryWhenThumbnailStorageReadFails() {
    final UUID id = UUID.randomUUID();
    final byte[] encrypted = {6};
    final var envelope =
        new AsyncJobEnvelope(id, AsyncJobType.PRODUCT_IMAGE_THUMBNAIL, encrypted, 9L, null);
    final var message =
        new OutboxMessage(id, AsyncJobType.PRODUCT_IMAGE_THUMBNAIL, encrypted, 9L, null);
    final var asset =
        MediaAsset.builder()
            .id(15L)
            .objectPath("temp/image.png")
            .thumbnailStatus(MediaThumbnailStatus.PENDING)
            .build();
    when(outboxRepository.lockById(id)).thenReturn(Optional.of(message));
    when(encryptionService.decrypt(encrypted)).thenReturn("{\"mediaAssetId\":15}".getBytes());
    when(mediaAssetService.findByIdOrThrow(15L)).thenReturn(asset);
    when(appConfiguration.getOutboxMaxAttempts()).thenReturn(3);
    when(appConfiguration.getOutboxRetryDelayMs()).thenReturn(1000);
    doThrow(new IllegalStateException("temporary storage failure"))
        .when(objectStorage)
        .download("temp/image.png");

    consumer.consume(envelope);

    assertEquals(OutboxStatus.PENDING, message.getStatus());
    assertEquals(1, message.getDeliveryAttempts());
    assertEquals(MediaThumbnailStatus.PENDING, asset.getThumbnailStatus());
  }
}
