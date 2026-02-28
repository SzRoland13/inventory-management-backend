package dev.roland.inventory_management_backend.service;

import java.util.Optional;

import dev.roland.inventory_management_backend.enums.MediaEntityType;
import dev.roland.inventory_management_backend.enums.MediaUsageType;
import dev.roland.inventory_management_backend.model.MediaUsage;

public interface MediaUsageService extends BaseService<MediaUsage, Long> {
  Optional<MediaUsage> findByEntityTypeAndEntityIdAndUsageType(
      MediaEntityType type, Long entityId, MediaUsageType usageType);

  Long usageCountByMediaAssetId(Long id);
}
