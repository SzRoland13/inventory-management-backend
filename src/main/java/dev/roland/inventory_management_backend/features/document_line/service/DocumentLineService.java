package dev.roland.inventory_management_backend.features.document_line.service;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;

/** Defines operations supported by the document line feature. */
public interface DocumentLineService extends BaseService<DocumentLine, Long> {
  /**
   * Checks whether a matching record exists.
   *
   * @param unitId the unit identifier
   * @return true if a matching record exists
   */
  boolean existsByUnitSnapshotId(Long unitId);
}
