package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.DocumentLine;
import dev.roland.inventory_management_backend.repository.DocumentLineRepository;
import dev.roland.inventory_management_backend.service.DocumentLineService;
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
