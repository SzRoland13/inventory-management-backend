package dev.roland.inventory_management_backend.features.media_asset;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaPreviewResponse;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaUploadInitRequest;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaUploadInitResponse;
import dev.roland.inventory_management_backend.features.media_asset.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.features.media_asset.message.MediaMessageKey;
import lombok.RequiredArgsConstructor;

/** Exposes media upload, preview, and deletion endpoints. */
@RestController
@RequestMapping(MediaAssetController.MEDIA_ASSET_BASE_ENDPOINT)
@RequiredArgsConstructor
public class MediaAssetController {
  public static final String MEDIA_ASSET_BASE_ENDPOINT = "api/v1/media";
  public static final String ID_PARAM = "/{id}";
  public static final String MEDIA_ASSET_PREVIEW_ENDPOINT = ID_PARAM + "/preview";

  private final MediaAssetFacade mediaAssetFacade;

  /**
   * Creates upload instructions for a new media asset.
   *
   * @param request metadata for the asset to upload
   * @return response containing the upload instructions
   */
  @PostMapping
  public ResponseEntity<ApiResponse<MediaUploadInitResponse>> initializeUpload(
      @RequestBody MediaUploadInitRequest request) {
    return ResponseEntity.accepted()
        .body(
            ApiResponse.success(
                MediaMessageKey.URL_GENERATED,
                mediaAssetFacade.getPutRequestForNewMediaAsset(request)));
  }

  /**
   * Returns a preview URL for the requested media asset.
   *
   * @param id identifier of the media asset
   * @return response containing the preview URL
   */
  @GetMapping(MEDIA_ASSET_PREVIEW_ENDPOINT)
  public ResponseEntity<ApiResponse<MediaPreviewResponse>> getPreview(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(MediaMessageKey.URL_GENERATED, mediaAssetFacade.getPreview(id)));
  }

  /**
   * Deletes the requested media asset.
   *
   * @param id identifier of the media asset to delete
   * @return deletion confirmation response
   */
  @DeleteMapping(ID_PARAM)
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    mediaAssetFacade.deleteAsset(id);

    return ResponseEntity.accepted().body(ApiResponse.success(MediaMessageKey.MEDIA_DELETED, null));
  }
}
