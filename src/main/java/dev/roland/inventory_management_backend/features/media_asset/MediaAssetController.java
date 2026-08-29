package dev.roland.inventory_management_backend.features.media_asset;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.media.MediaPreviewResponse;
import dev.roland.inventory_management_backend.dto.media.MediaUploadInitRequest;
import dev.roland.inventory_management_backend.dto.media.MediaUploadInitResponse;
import dev.roland.inventory_management_backend.features.media_asset.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.features.media_asset.message.MediaMessageKey;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(MediaAssetController.MEDIA_ASSET_BASE_ENDPOINT)
@RequiredArgsConstructor
public class MediaAssetController {
  public static final String MEDIA_ASSET_BASE_ENDPOINT = "api/v1/media";
  public static final String ID_PARAM = "/{id}";
  public static final String MEDIA_ASSET_PREVIEW_ENDPOINT = ID_PARAM + "/preview";

  private final MediaAssetFacade mediaAssetFacade;

  @PostMapping
  public ResponseEntity<ApiResponse<MediaUploadInitResponse>> initializeUpload(
      @RequestBody MediaUploadInitRequest request) {
    return ResponseEntity.accepted()
        .body(
            ApiResponse.success(
                MediaMessageKey.URL_GENERATED,
                mediaAssetFacade.getPutRequestForNewMediaAsset(request)));
  }

  @GetMapping(MEDIA_ASSET_PREVIEW_ENDPOINT)
  public ResponseEntity<ApiResponse<MediaPreviewResponse>> getPreview(@PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(MediaMessageKey.URL_GENERATED, mediaAssetFacade.getPreview(id)));
  }

  @DeleteMapping(ID_PARAM)
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    mediaAssetFacade.deleteAsset(id);

    return ResponseEntity.accepted().body(ApiResponse.success(MediaMessageKey.MEDIA_DELETED, null));
  }
}
