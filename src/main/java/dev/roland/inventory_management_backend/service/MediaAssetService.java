package dev.roland.inventory_management_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import dev.roland.inventory_management_backend.model.MediaAsset;

public interface MediaAssetService extends BaseService<MediaAsset, Long> {
  List<MediaAsset> findOrphanAssetsOlderThan(LocalDateTime threshold);
}
