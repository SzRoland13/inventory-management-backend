package dev.roland.inventory_management_backend.features.media_asset.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.dto.GeneratedMediaPathAndName;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaUploadInitRequest;
import dev.roland.inventory_management_backend.features.media_asset.dto.PresignedUrlData;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;

@ExtendWith(MockitoExtension.class)
class MediaAssetFacadeImplTest {

  @Mock private MediaAssetService mediaAssetService;
  @Mock private ObjectStorageService objectStorageService;

  private MediaAssetFacadeImpl facade;

  @BeforeEach
  void setUp() {
    facade = new MediaAssetFacadeImpl(mediaAssetService, objectStorageService);
  }

  @Test
  void initializesUploadUsingGeneratedPathAndPresignedPutUrl() {
    final MediaUploadInitRequest request = new MediaUploadInitRequest();
    request.setFilename("manual.pdf");
    request.setMimeType("application/pdf");
    request.setFileSize(42L);
    when(objectStorageService.generateTempObjectPath("manual.pdf"))
        .thenReturn(
            GeneratedMediaPathAndName.builder()
                .objectPath("temp/generated.pdf")
                .filename("generated.pdf")
                .build());
    when(mediaAssetService.save(any(MediaAsset.class)))
        .thenAnswer(
            invocation -> {
              final MediaAsset asset = invocation.getArgument(0);
              asset.setId(17L);
              return asset;
            });
    final Instant expiry = Instant.parse("2030-01-01T00:00:00Z");
    when(objectStorageService.generatePresignedPutUrl("temp/generated.pdf", "application/pdf"))
        .thenReturn(
            PresignedUrlData.builder().url("https://storage/upload").expiry(expiry).build());

    final var response = facade.getPutRequestForNewMediaAsset(request);

    assertEquals(17L, response.getId());
    assertEquals("https://storage/upload", response.getPutUrl());
    assertEquals(expiry, response.getExpiry());
    verify(mediaAssetService).save(any(MediaAsset.class));
  }

  @Test
  void previewSignsTheStoredObjectPathAndDeleteRemovesObjectAndAsset() {
    final MediaAsset asset =
        MediaAsset.builder().id(9L).objectPath("users/9/avatar/photo.png").build();
    when(mediaAssetService.findByIdOrThrow(9L)).thenReturn(asset);
    when(objectStorageService.generatePresignedGetUrl(asset.getObjectPath()))
        .thenReturn(
            PresignedUrlData.builder()
                .url("https://storage/photo")
                .expiry(Instant.parse("2030-01-01T00:00:00Z"))
                .build());

    final var preview = facade.getPreview(9L);

    assertEquals(9L, preview.getId());
    assertEquals("https://storage/photo", preview.getGetUrl());
    facade.deleteAsset(9L);

    verify(objectStorageService).delete(asset.getObjectPath());
    verify(mediaAssetService).delete(asset);
  }
}
