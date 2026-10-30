package dev.roland.inventory_management_backend.features.media_usage.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.repository.MediaUsageRepository;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

/** Implements the media usage service operations. */
@Service
@RequiredArgsConstructor
public class MediaUsageServiceImpl implements MediaUsageService {
  private final MediaUsageRepository mediaUsageRepository;

  /** {@inheritDoc} */
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP",
      justification =
          "BaseService CRUD methods use this accessor to reach the Spring Data repository.")
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
      final MediaEntityType type, final Long entityId, final MediaUsageType usageType) {
    return mediaUsageRepository.findByEntityTypeAndEntityIdAndUsageType(type, entityId, usageType);
  }

  @Override
  public List<MediaUsage> findAllByEntityTypeAndEntityIdAndUsageTypeOrderBySortOrderAsc(
      final MediaEntityType type, final Long entityId, final MediaUsageType usageType) {
    return mediaUsageRepository.findAllByEntityTypeAndEntityIdAndUsageTypeOrderBySortOrderAsc(
        type, entityId, usageType);
  }

  @Override
  public List<MediaUsage>
      findAllByEntityTypeAndEntityIdInAndUsageTypeOrderByEntityIdAscSortOrderAsc(
          final MediaEntityType type, final List<Long> entityIds, final MediaUsageType usageType) {
    return mediaUsageRepository
        .findAllByEntityTypeAndEntityIdInAndUsageTypeOrderByEntityIdAscSortOrderAsc(
            type, entityIds, usageType);
  }

  @Override
  public void deleteAllByEntityTypeAndEntityIdAndUsageType(
      final MediaEntityType type, final Long entityId, final MediaUsageType usageType) {
    mediaUsageRepository.deleteAllByEntityTypeAndEntityIdAndUsageType(type, entityId, usageType);
  }

  /**
   * {@inheritDoc}
   *
   * @param id id supplied to this method
   * @return usage count by media asset id result
   */
  @Override
  public Long usageCountByMediaAssetId(final Long id) {
    return mediaUsageRepository.countByMediaAssetId(id);
  }
}
