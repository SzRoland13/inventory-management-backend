package dev.roland.inventory_management_backend.service.implementation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.enums.MediaEntityType;
import dev.roland.inventory_management_backend.enums.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.repository.MediaUsageRepository;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaUsageServiceImpl implements MediaUsageService {
  private final MediaUsageRepository mediaUsageRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<MediaUsage, Long> getRepository() {
    return mediaUsageRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.MEDIA_USAGE;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<MediaUsage> findByEntityTypeAndEntityIdAndUsageType(
      MediaEntityType type, Long entityId, MediaUsageType usageType) {
    return mediaUsageRepository.findByEntityTypeAndEntityIdAndUsageType(type, entityId, usageType);
  }

  /** {@inheritDoc} */
  @Override
  public Long usageCountByMediaAssetId(Long id) {
    return mediaUsageRepository.countByMediaAsset_Id(id);
  }
}
