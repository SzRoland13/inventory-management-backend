package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.DocumentRelation;
import dev.roland.inventory_management_backend.repository.DocumentRelationRepository;
import dev.roland.inventory_management_backend.service.DocumentRelationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentRelationServiceImpl implements DocumentRelationService {
  private final DocumentRelationRepository documentRelationRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<DocumentRelation, Long> getRepository() {
    return documentRelationRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.DOCUMENT_RELATION;
  }
}
