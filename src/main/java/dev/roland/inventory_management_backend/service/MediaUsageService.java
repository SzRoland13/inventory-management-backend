package dev.roland.inventory_management_backend.service;

import java.util.Optional;

import dev.roland.inventory_management_backend.enums.MediaEntityType;
import dev.roland.inventory_management_backend.enums.MediaUsageType;
import dev.roland.inventory_management_backend.model.MediaUsage;

public interface MediaUsageService extends BaseService<MediaUsage, Long> {
  /**
   * Finds the media usage attached to an entity for a specific usage type.
   *
   * @param type entity type that owns the usage
   * @param entityId owning entity id
   * @param usageType usage role of the media asset
   * @return matching usage when one exists
   */
  Optional<MediaUsage> findByEntityTypeAndEntityIdAndUsageType(
      MediaEntityType type, Long entityId, MediaUsageType usageType);

  /**
   * Counts how many usage records reference a media asset.
   *
   * @param id media asset id
   * @return number of usages referencing the media asset
   */
  Long usageCountByMediaAssetId(Long id);
}
