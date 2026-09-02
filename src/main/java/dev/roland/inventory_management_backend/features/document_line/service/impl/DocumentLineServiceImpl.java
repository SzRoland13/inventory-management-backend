package dev.roland.inventory_management_backend.features.document_line.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.repository.DocumentLineRepository;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentLineServiceImpl implements DocumentLineService {
  private final DocumentLineRepository documentLineRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<DocumentLine, Long> getRepository() {
    return documentLineRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.DOCUMENT_LINE;
  }
}
