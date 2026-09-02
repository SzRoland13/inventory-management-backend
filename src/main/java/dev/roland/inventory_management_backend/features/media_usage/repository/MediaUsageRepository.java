package dev.roland.inventory_management_backend.features.media_usage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;

@Repository
public interface MediaUsageRepository extends JpaRepository<MediaUsage, Long> {
  @Query(
      """
      SELECT u FROM MediaUsage u
      WHERE u.entityType = :type AND u.entityId = :entityId AND u.usageType = :usageType
      """)
  Optional<MediaUsage> findByEntityTypeAndEntityIdAndUsageType(
      MediaEntityType type, Long entityId, MediaUsageType usageType);

  Long countByMediaAsset_Id(Long id);
}
