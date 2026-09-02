package dev.roland.inventory_management_backend.features.media_asset.facade.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.dto.GeneratedMediaPathAndName;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaPreviewResponse;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaUploadInitRequest;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaUploadInitResponse;
import dev.roland.inventory_management_backend.features.media_asset.dto.PresignedUrlData;
import dev.roland.inventory_management_backend.features.media_asset.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaAssetFacadeImpl implements MediaAssetFacade {
  private final MediaAssetService mediaAssetService;
  private final ObjectStorageService objectStorageService;

  /** {@inheritDoc} */
  @Override
  public MediaUploadInitResponse getPutRequestForNewMediaAsset(MediaUploadInitRequest request) {
    GeneratedMediaPathAndName generatedMediaPathAndName =
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

    PresignedUrlData presigned =
        objectStorageService.generatePresignedPutUrl(
            mediaAsset.getObjectPath(), request.getMimeType());

    return new MediaUploadInitResponse(
        mediaAsset.getId(), presigned.getUrl(), presigned.getExpiry());
  }

  /** {@inheritDoc} */
  @Override
  public MediaPreviewResponse getPreview(Long id) {
    MediaAsset mediaAsset = mediaAssetService.findByIdOrThrow(id);
    PresignedUrlData presigned =
        objectStorageService.generatePresignedGetUrl(mediaAsset.getObjectPath());

    return new MediaPreviewResponse(mediaAsset.getId(), presigned.getUrl(), presigned.getExpiry());
  }

  /** {@inheritDoc} */
  @Override
  public void deleteAsset(Long id) {
    MediaAsset mediaAsset = mediaAssetService.findByIdOrThrow(id);

    objectStorageService.delete(mediaAsset.getObjectPath());
    mediaAssetService.delete(mediaAsset);
  }
}
