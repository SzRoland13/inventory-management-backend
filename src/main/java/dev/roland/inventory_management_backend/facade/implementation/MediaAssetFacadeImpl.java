package dev.roland.inventory_management_backend.facade.implementation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.dto.media.GeneratedMediaPathAndName;
import dev.roland.inventory_management_backend.dto.media.MediaPreviewResponse;
import dev.roland.inventory_management_backend.dto.media.MediaUploadInitRequest;
import dev.roland.inventory_management_backend.dto.media.MediaUploadInitResponse;
import dev.roland.inventory_management_backend.dto.media.PresignedUrlData;
import dev.roland.inventory_management_backend.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.model.MediaAsset;
import dev.roland.inventory_management_backend.service.MediaAssetService;
import dev.roland.inventory_management_backend.service.common.ObjectStorageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaAssetFacadeImpl implements MediaAssetFacade {
  private final MediaAssetService mediaAssetService;
  private final ObjectStorageService objectStorageService;

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

  @Override
  public MediaPreviewResponse getPreview(Long id) {
    MediaAsset mediaAsset = mediaAssetService.findByIdOrThrow(id);
    PresignedUrlData presigned =
        objectStorageService.generatePresignedGetUrl(mediaAsset.getObjectPath());

    return new MediaPreviewResponse(mediaAsset.getId(), presigned.getUrl(), presigned.getExpiry());
  }

  @Override
  public void deleteAsset(Long id) {
    MediaAsset mediaAsset = mediaAssetService.findByIdOrThrow(id);

    objectStorageService.delete(mediaAsset.getObjectPath());
    mediaAssetService.delete(mediaAsset);
  }
}
