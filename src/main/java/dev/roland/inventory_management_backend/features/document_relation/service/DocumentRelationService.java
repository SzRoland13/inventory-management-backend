package dev.roland.inventory_management_backend.features.document_relation.service;

import java.util.List;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;

/** Defines operations supported by the document relationship feature. */
public interface DocumentRelationService extends BaseService<DocumentRelation, Long> {
  /**
   * Lists relations where the document is either the source or target.
   *
   * @param sourceId source document identifier
   * @param targetId target document identifier
   * @return matching relations
   */
  List<DocumentRelation> findAllBySourceDocumentIdOrTargetDocumentId(Long sourceId, Long targetId);
}
