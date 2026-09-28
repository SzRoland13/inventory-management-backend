package dev.roland.inventory_management_backend.features.document_line.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.document_line.DocumentLine;

/** Provides database queries for document line records. */
public interface DocumentLineRepository extends JpaRepository<DocumentLine, Long> {
  /**
   * Checks whether a matching record exists.
   *
   * @param unitId the unit identifier
   * @return true if a matching record exists
   */
  boolean existsByUnitSnapshotId(Long unitId);
}
