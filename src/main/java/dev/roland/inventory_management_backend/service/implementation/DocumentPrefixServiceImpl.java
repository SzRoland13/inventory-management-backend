package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.messageKey.MessageKey;
import dev.roland.inventory_management_backend.messageKey.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.DocumentPrefix;
import dev.roland.inventory_management_backend.repository.DocumentPrefixRepository;
import dev.roland.inventory_management_backend.service.DocumentPrefixService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentPrefixServiceImpl implements DocumentPrefixService {
  private final DocumentPrefixRepository documentPrefixRepository;

  @Override
  public JpaRepository<DocumentPrefix, Long> getRepository() {
    return documentPrefixRepository;
  }

  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.DOCUMENT_PREFIX;
  }
}
