package dev.roland.inventory_management_backend.features.media_asset.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;

/** Defines operations supported by the media asset feature. */
public interface MediaAssetService extends BaseService<MediaAsset, Long> {
  /**
   * Finds an asset without throwing when it has already been deleted.
   *
   * @param id media asset identifier
   * @return the asset when it exists
   */
  Optional<MediaAsset> findById(Long id);

  /**
   * Finds media assets that are not attached to an entity and were created before the threshold.
   *
   * @param threshold assets older than this timestamp are eligible
   * @return orphaned media assets older than the threshold
   */
  List<MediaAsset> findOrphanAssetsOlderThan(LocalDateTime threshold);
}
