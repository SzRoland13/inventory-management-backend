package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.repository.DocumentRepository;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
  private final DocumentRepository documentRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<Document, Long> getRepository() {
    return documentRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.DOCUMENT;
  }
}
