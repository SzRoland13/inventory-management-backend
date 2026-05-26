package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.DocumentSequence;
import dev.roland.inventory_management_backend.repository.DocumentSequenceRepository;
import dev.roland.inventory_management_backend.service.DocumentSequenceService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentSequenceServiceImpl implements DocumentSequenceService {
  private final DocumentSequenceRepository documentSequenceRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<DocumentSequence, Long> getRepository() {
    return documentSequenceRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.DOCUMENT_SEQUENCE;
  }
}
