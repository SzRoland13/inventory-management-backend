package dev.roland.inventory_management_backend.features.media_asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.features.media_asset.dto.MediaPreviewResponse;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaUploadInitRequest;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaUploadInitResponse;
import dev.roland.inventory_management_backend.features.media_asset.facade.MediaAssetFacade;

@ExtendWith(MockitoExtension.class)
class MediaAssetControllerTest {

  @Mock private MediaAssetFacade mediaAssetFacade;

  private MediaAssetController controller;

  @BeforeEach
  void setUp() {
    controller = new MediaAssetController(mediaAssetFacade);
  }

  @Test
  void createsUploadInstructionsAndReturnsAccepted() {
    final MediaUploadInitRequest request = new MediaUploadInitRequest();
    final MediaUploadInitResponse response = new MediaUploadInitResponse(1L, "https://put", null);
    when(mediaAssetFacade.getPutRequestForNewMediaAsset(request)).thenReturn(response);

    final var result = controller.initializeUpload(request);

    assertEquals(202, result.getStatusCode().value());
    assertEquals(response, result.getBody().getPayload());
    verify(mediaAssetFacade).getPutRequestForNewMediaAsset(request);
  }

  @Test
  void returnsPreviewAndAcceptsDeletion() {
    final MediaPreviewResponse preview = new MediaPreviewResponse(5L, "https://get", null);
    when(mediaAssetFacade.getPreview(5L)).thenReturn(preview);

    assertEquals(preview, controller.getPreview(5L).getBody().getPayload());
    assertEquals(202, controller.delete(5L).getStatusCode().value());
    verify(mediaAssetFacade).deleteAsset(5L);
  }
}
