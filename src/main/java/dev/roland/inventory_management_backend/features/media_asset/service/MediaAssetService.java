package dev.roland.inventory_management_backend.features.media_asset.service;

import java.time.LocalDateTime;
import java.util.List;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;

public interface MediaAssetService extends BaseService<MediaAsset, Long> {
  /**
   * Finds media assets that are not attached to an entity and were created before the threshold.
   *
   * @param threshold assets older than this timestamp are eligible
   * @return orphaned media assets older than the threshold
   */
  List<MediaAsset> findOrphanAssetsOlderThan(LocalDateTime threshold);
}
