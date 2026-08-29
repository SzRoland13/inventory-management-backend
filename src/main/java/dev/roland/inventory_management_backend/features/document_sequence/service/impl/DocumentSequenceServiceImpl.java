package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.document_sequence.DocumentSequence;
import dev.roland.inventory_management_backend.features.document_sequence.repository.DocumentSequenceRepository;
import dev.roland.inventory_management_backend.features.document_sequence.service.DocumentSequenceService;
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
