package dev.roland.inventory_management_backend.features.document_relation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;

/** Provides database queries for document relationship records. */
public interface DocumentRelationRepository extends JpaRepository<DocumentRelation, Long> {
  List<DocumentRelation> findAllBySourceDocumentIdOrTargetDocumentId(Long sourceId, Long targetId);
}
