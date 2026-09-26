package dev.roland.inventory_management_backend.common.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;

@ExtendWith(MockitoExtension.class)
class MediaCleanupServiceTest {

  @Mock private MediaAssetService mediaAssetService;
  @Mock private ObjectStorageService objectStorageService;

  private MediaCleanupService service;

  @BeforeEach
  void setUp() {
    service = new MediaCleanupService(mediaAssetService, objectStorageService);
  }

  @Test
  void deletesEachOldOrphanFromStorageAndDatabase() {
    final MediaAsset first = MediaAsset.builder().id(1L).objectPath("temp/one").build();
    final MediaAsset second = MediaAsset.builder().id(2L).objectPath("temp/two").build();
    when(mediaAssetService.findOrphanAssetsOlderThan(any())).thenReturn(List.of(first, second));

    service.cleanupOrphanedMedia();

    verify(objectStorageService).delete("temp/one");
    verify(objectStorageService).delete("temp/two");
    verify(mediaAssetService).deleteById(1L);
    verify(mediaAssetService).deleteById(2L);
  }

  @Test
  void continuesAfterAStorageFailureAndDoesNothingForEmptyResults() {
    final MediaAsset failed = MediaAsset.builder().id(1L).objectPath("temp/failed").build();
    final MediaAsset successful = MediaAsset.builder().id(2L).objectPath("temp/good").build();
    when(mediaAssetService.findOrphanAssetsOlderThan(any()))
        .thenReturn(List.of(failed, successful), List.of());
    org.mockito.Mockito.doThrow(new IllegalStateException("storage unavailable"))
        .when(objectStorageService)
        .delete("temp/failed");

    service.cleanupOrphanedMedia();
    service.cleanupOrphanedMedia();

    verify(mediaAssetService, never()).deleteById(1L);
    verify(mediaAssetService).deleteById(2L);
  }
}
