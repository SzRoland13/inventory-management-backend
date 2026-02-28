package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.media.MediaPreviewResponse;
import dev.roland.inventory_management_backend.dto.media.MediaUploadInitRequest;
import dev.roland.inventory_management_backend.dto.media.MediaUploadInitResponse;

public interface MediaAssetFacade {
  MediaUploadInitResponse getPutRequestForNewMediaAsset(MediaUploadInitRequest request);

  MediaPreviewResponse getPreview(Long id);

  void deleteAsset(Long id);
}
