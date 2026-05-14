package dev.roland.inventory_management_backend.service.common;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.model.MediaAsset;
import dev.roland.inventory_management_backend.service.MediaAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaCleanupService {
  private final MediaAssetService mediaAssetService;
  private final ObjectStorageService objectStorageService;

  @Scheduled(cron = "0 0 2 * * ?") // 2 AM Every day
  @Transactional
  public void cleanupOrphanedMedia() {
    LocalDateTime threshold = LocalDateTime.now().minusHours(24);
    List<MediaAsset> orphanedAssets = mediaAssetService.findOrphanAssetsOlderThan(threshold);

    log.info("Found {} orphaned media assets to delete", orphanedAssets.size());

    for (MediaAsset asset : orphanedAssets) {
      try {
        objectStorageService.delete(asset.getObjectPath());
        mediaAssetService.deleteById(asset.getId());
        log.debug("Deleted orphaned media asset: {}", asset.getId());
      } catch (Exception e) {
        log.error("Failed to delete media asset: {}", asset.getId(), e);
      }
    }
  }
}
