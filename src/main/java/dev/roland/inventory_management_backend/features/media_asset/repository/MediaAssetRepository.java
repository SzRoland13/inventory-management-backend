package dev.roland.inventory_management_backend.features.media_asset.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;

/** Finds media assets and identifies old unreferenced uploads. */
@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
  /**
   * Finds unused media assets older than the supplied cutoff.
   *
   * @param threshold oldest creation time to include
   * @return unused media assets created before the threshold
   */
  @Query(
      """
      SELECT m FROM MediaAsset m
      WHERE m.createdAt < :threshold
      AND NOT EXISTS (
      SELECT u FROM MediaUsage u
      WHERE u.mediaAsset = m
      )
      """)
  List<MediaAsset> findOrphanAssetsOlderThan(LocalDateTime threshold);
}
