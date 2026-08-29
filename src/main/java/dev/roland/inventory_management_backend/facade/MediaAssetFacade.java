package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.media.MediaPreviewResponse;
import dev.roland.inventory_management_backend.dto.media.MediaUploadInitRequest;
import dev.roland.inventory_management_backend.dto.media.MediaUploadInitResponse;

/** Coordinates media upload, preview, and deletion operations. */
public interface MediaAssetFacade {
  /**
   * Initializes an upload and returns a presigned media upload request.
   *
   * @param request upload metadata and file information
   * @return initialized upload details and presigned URL
   */
  MediaUploadInitResponse getPutRequestForNewMediaAsset(MediaUploadInitRequest request);

  /**
   * Returns a preview URL for the media asset with the given identifier.
   *
   * @param id media asset identifier
   * @return media preview details
   */
  MediaPreviewResponse getPreview(Long id);

  /**
   * Deletes the media asset with the given identifier when it is no longer in use.
   *
   * @param id media asset identifier
   */
  void deleteAsset(Long id);
}
