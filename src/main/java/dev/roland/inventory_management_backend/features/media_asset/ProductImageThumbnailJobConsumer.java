package dev.roland.inventory_management_backend.features.media_asset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;

import jakarta.transaction.Transactional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.roland.inventory_management_backend.common.async.AsyncJobEnvelope;
import dev.roland.inventory_management_backend.common.async.AsyncJobType;
import dev.roland.inventory_management_backend.common.async.OutboxEncryptionService;
import dev.roland.inventory_management_backend.common.async.OutboxMessage;
import dev.roland.inventory_management_backend.common.async.OutboxMessageRepository;
import dev.roland.inventory_management_backend.common.async.OutboxMessageSupport;
import dev.roland.inventory_management_backend.common.async.OutboxStatus;
import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.media_asset.dto.ProductImageThumbnailJobPayload;
import dev.roland.inventory_management_backend.features.media_asset.message.MediaMessageKey;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_asset.service.ProductThumbnailService;
import dev.roland.inventory_management_backend.features.media_asset.service.ThumbnailImageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Generates stored WebP thumbnails for product image assets. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductImageThumbnailJobConsumer {
  private final OutboxMessageRepository outboxRepository;
  private final OutboxEncryptionService encryptionService;
  private final ObjectMapper objectMapper;
  private final MediaAssetService mediaAssetService;
  private final ObjectStorageService objectStorage;
  private final ThumbnailImageProcessor imageProcessor;
  private final AppConfiguration appConfiguration;
  private final ProductThumbnailService productThumbnailService;

  /** Processes a queued thumbnail job. */
  @RabbitListener(
      queues =
          "#{T(dev.roland.inventory_management_backend.common.async.AsyncJobType).PRODUCT_IMAGE_THUMBNAIL"
              + ".getQueueName()}")
  @Transactional
  public void consume(final AsyncJobEnvelope envelope) {
    final OutboxMessage message =
        outboxRepository
            .lockById(envelope.id())
            .orElseThrow(
                () -> new MediaThumbnailException(MediaMessageKey.THUMBNAIL_JOB_NOT_FOUND, true));
    if (OutboxMessageSupport.isTerminal(message)
        || (message.getNextAttemptAt() != null
            && message.getNextAttemptAt().isAfter(Instant.now()))) {
      return;
    }
    try {
      if (!OutboxMessageSupport.matchesEnvelope(message, envelope)
          || message.getJobType() != AsyncJobType.PRODUCT_IMAGE_THUMBNAIL) {
        throw new MediaThumbnailException(MediaMessageKey.THUMBNAIL_JOB_INVALID, true);
      }
      final ProductImageThumbnailJobPayload payload =
          objectMapper.readValue(
              encryptionService.decrypt(message.getEncryptedPayload()),
              ProductImageThumbnailJobPayload.class);
      if (payload.mediaAssetId() == null || payload.mediaAssetId() <= 0) {
        throw new MediaThumbnailException(MediaMessageKey.THUMBNAIL_PAYLOAD_INVALID, true);
      }
      final long assetId = payload.mediaAssetId();
      final MediaAsset asset = mediaAssetService.findByIdOrThrow(assetId);
      final String thumbnailPath = productThumbnailService.objectPath(assetId);
      final byte[] thumbnail =
          imageProcessor.createThumbnail(objectStorage.download(asset.getObjectPath()));
      objectStorage.upload(
          thumbnailPath,
          thumbnail.length,
          ProductImageFormat.WEBP.getMimeType(),
          new ByteArrayInputStream(thumbnail));
      asset.setThumbnailObjectPath(thumbnailPath);
      asset.setThumbnailStatus(MediaThumbnailStatus.READY);
      mediaAssetService.save(asset);
      message.setStatus(OutboxStatus.SENT);
      message.setEncryptedPayload(new byte[0]);
      message.setLastError(null);
      message.setUpdatedAt(Instant.now());
    } catch (RuntimeException | IOException error) {
      final int attempts = message.getDeliveryAttempts() + 1;
      message.setDeliveryAttempts(attempts);
      message.setLastError(diagnostic(error));
      message.setUpdatedAt(Instant.now());
      final Long assetId = extractAssetId(message);
      final boolean permanent =
          error instanceof MediaThumbnailException thumbnailException
              && thumbnailException.isPermanent();
      if (permanent || attempts >= Math.max(1, appConfiguration.getOutboxMaxAttempts())) {
        message.setStatus(OutboxStatus.DLQ_PENDING);
        if (assetId != null) {
          mediaAssetService
              .findById(assetId)
              .ifPresent(asset -> asset.setThumbnailStatus(MediaThumbnailStatus.FAILED));
        }
        log.error("Product thumbnail job {} failed permanently", message.getId(), error);
      } else {
        message.setStatus(OutboxStatus.PENDING);
        message.setNextAttemptAt(
            Instant.now().plusMillis(Math.max(1, appConfiguration.getOutboxRetryDelayMs())));
        log.warn("Product thumbnail job {} will retry", message.getId(), error);
      }
      message.setLeaseToken(null);
      message.setLockedUntil(null);
    }
  }

  private Long extractAssetId(final OutboxMessage message) {
    try {
      final ProductImageThumbnailJobPayload payload =
          objectMapper.readValue(
              encryptionService.decrypt(message.getEncryptedPayload()),
              ProductImageThumbnailJobPayload.class);
      return payload.mediaAssetId();
    } catch (RuntimeException | IOException ignored) {
      return null;
    }
  }

  private String diagnostic(final Throwable error) {
    final String detail =
        error instanceof MediaThumbnailException thumbnailException
            ? thumbnailException.getMessageKey().getKey()
            : MediaMessageKey.THUMBNAIL_GENERATION_FAILED.getKey();
    return detail.substring(0, Math.min(1000, detail.length()));
  }
}
