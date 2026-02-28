package dev.roland.inventory_management_backend.service.implementation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.messageKey.MessageKey;
import dev.roland.inventory_management_backend.messageKey.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.MediaAsset;
import dev.roland.inventory_management_backend.repository.MediaAssetRepository;
import dev.roland.inventory_management_backend.service.MediaAssetService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaAssetServiceImpl implements MediaAssetService {
  private final MediaAssetRepository mediaAssetRepository;

  @Override
  public JpaRepository<MediaAsset, Long> getRepository() {
    return mediaAssetRepository;
  }

  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.MEDIA_ASSET;
  }

  @Override
  public List<MediaAsset> findOrphanAssetsOlderThan(LocalDateTime threshold) {
    return mediaAssetRepository.findOrphanAssetsOlderThan(threshold);
  }
}
