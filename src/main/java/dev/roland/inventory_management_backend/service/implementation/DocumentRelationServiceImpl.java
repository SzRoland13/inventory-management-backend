package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;
import dev.roland.inventory_management_backend.features.document_relation.repository.DocumentRelationRepository;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
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
