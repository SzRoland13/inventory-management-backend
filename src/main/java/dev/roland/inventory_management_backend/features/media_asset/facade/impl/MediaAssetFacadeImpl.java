package dev.roland.inventory_management_backend.features.media_asset.facade.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.MediaThumbnailStatus;
import dev.roland.inventory_management_backend.features.media_asset.ProductImageFormat;
import dev.roland.inventory_management_backend.features.media_asset.dto.GeneratedMediaPathAndName;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaPreviewResponse;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaThumbnailPreviewResponse;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaUploadInitRequest;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaUploadInitResponse;
import dev.roland.inventory_management_backend.features.media_asset.dto.PresignedUrlData;
import dev.roland.inventory_management_backend.features.media_asset.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_asset.service.ProductThumbnailService;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import lombok.RequiredArgsConstructor;

/** Implements the media asset service operations. */
@Service
@RequiredArgsConstructor
public class MediaAssetFacadeImpl implements MediaAssetFacade {
  private final MediaAssetService mediaAssetService;
  private final ObjectStorageService objectStorageService;
  private final MediaUsageService mediaUsageService;
  private final ProductThumbnailService productThumbnailService;

  /**
   * {@inheritDoc}
   *
   * @param request request supplied to this method
   * @return get put request for new media asset result
   */
  @Override
  public MediaUploadInitResponse getPutRequestForNewMediaAsset(
      final MediaUploadInitRequest request) {
    final GeneratedMediaPathAndName generatedMediaPathAndName =
        objectStorageService.generateTempObjectPath(request.getFilename());

    MediaAsset mediaAsset =
        MediaAsset.builder()
            .objectPath(generatedMediaPathAndName.getObjectPath())
            .mimeType(request.getMimeType())
            .filename(generatedMediaPathAndName.getFilename())
            .fileSize(request.getFileSize())
            .createdAt(LocalDateTime.now())
            .build();

    mediaAsset = mediaAssetService.save(mediaAsset);

    final PresignedUrlData presigned =
        objectStorageService.generatePresignedPutUrl(
            mediaAsset.getObjectPath(), request.getMimeType());

    return new MediaUploadInitResponse(
        mediaAsset.getId(), presigned.getUrl(), presigned.getExpiry());
  }

  /** {@inheritDoc} */
  @Override
  public MediaPreviewResponse getPreview(final Long id) {
    final MediaAsset mediaAsset = mediaAssetService.findByIdOrThrow(id);
    final PresignedUrlData presigned =
        objectStorageService.generatePresignedGetUrl(mediaAsset.getObjectPath());

    return new MediaPreviewResponse(mediaAsset.getId(), presigned.getUrl(), presigned.getExpiry());
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public MediaThumbnailPreviewResponse getThumbnailPreview(final Long id) {
    final MediaAsset asset = mediaAssetService.findByIdOrThrow(id);
    if (asset.getThumbnailStatus() == null
        && ProductImageFormat.supports(asset.getMimeType())
        && mediaUsageService.existsByMediaAssetIdAndEntityTypeAndUsageType(
            id, MediaEntityType.PRODUCT, MediaUsageType.IMAGE)) {
      if (productThumbnailService.enqueueIfNeeded(asset, null)) {
        mediaAssetService.save(asset);
      }
    }
    final boolean ready =
        asset.getThumbnailStatus() == MediaThumbnailStatus.READY
            && asset.getThumbnailObjectPath() != null;
    if (!ready) {
      return new MediaThumbnailPreviewResponse(
          id,
          asset.getThumbnailStatus() == null
              ? MediaThumbnailStatus.FAILED
              : asset.getThumbnailStatus(),
          null,
          null);
    }
    final PresignedUrlData presigned =
        objectStorageService.generatePresignedGetUrl(asset.getThumbnailObjectPath());
    return new MediaThumbnailPreviewResponse(
        id, MediaThumbnailStatus.READY, presigned.getUrl(), presigned.getExpiry());
  }

  /**
   * {@inheritDoc}
   *
   * @param id id supplied to this method
   */
  @Override
  public void deleteAsset(final Long id) {
    final MediaAsset mediaAsset = mediaAssetService.findByIdOrThrow(id);

    objectStorageService.delete(mediaAsset.getObjectPath());
    if (mediaAsset.getThumbnailObjectPath() != null) {
      objectStorageService.delete(mediaAsset.getThumbnailObjectPath());
    }
    mediaAssetService.delete(mediaAsset);
  }
}
