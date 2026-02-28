package dev.roland.inventory_management_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.model.MediaAsset;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
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
