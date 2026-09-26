package dev.roland.inventory_management_backend.features.media_usage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;

/** Finds and counts links between media assets and owning entities. */
@Repository
public interface MediaUsageRepository extends JpaRepository<MediaUsage, Long> {
  /**
   * Finds media usage records for the specified entity and purpose.
   *
   * @param type owning entity type
   * @param entityId identifier of the owning entity
   * @param usageType purpose for which the media is used
   * @return matching usage record, if one exists
   */
  @Query(
      """
      SELECT u FROM MediaUsage u
      WHERE u.entityType = :type AND u.entityId = :entityId AND u.usageType = :usageType
      """)
  Optional<MediaUsage> findByEntityTypeAndEntityIdAndUsageType(
      MediaEntityType type, Long entityId, MediaUsageType usageType);

  /**
   * Counts the usages referencing a media asset.
   *
   * @param id identifier of the media asset
   * @return number of usage records referencing the asset
   */
  @Query("select count(u) from MediaUsage u where u.mediaAsset.id = ?1")
  Long countByMediaAssetId(Long id);
}
