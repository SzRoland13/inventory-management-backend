package dev.roland.inventory_management_backend.features.media_asset.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.repository.MediaAssetRepository;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaAssetServiceImpl implements MediaAssetService {
  private final MediaAssetRepository mediaAssetRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<MediaAsset, Long> getRepository() {
    return mediaAssetRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.MEDIA_ASSET;
  }

  /** {@inheritDoc} */
  @Override
  public List<MediaAsset> findOrphanAssetsOlderThan(LocalDateTime threshold) {
    return mediaAssetRepository.findOrphanAssetsOlderThan(threshold);
  }
}
