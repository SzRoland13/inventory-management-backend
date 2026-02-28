package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.messageKey.MessageKey;
import dev.roland.inventory_management_backend.messageKey.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.MediaUsage;
import dev.roland.inventory_management_backend.repository.MediaUsageRepository;
import dev.roland.inventory_management_backend.service.MediaUsageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaUsageServiceImpl implements MediaUsageService {
  private final MediaUsageRepository mediaUsageRepository;

  @Override
  public JpaRepository<MediaUsage, Long> getRepository() {
    return mediaUsageRepository;
  }

  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.MEDIA_USAGE;
  }
}
