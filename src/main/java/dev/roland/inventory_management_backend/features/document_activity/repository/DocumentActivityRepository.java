package dev.roland.inventory_management_backend.features.document_activity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.document_activity.DocumentActivity;

/** Reads document audit history. */
public interface DocumentActivityRepository extends JpaRepository<DocumentActivity, Long> {
  /**
   * Finds records matching the supplied criteria.
   *
   * @param documentId document identifier value
   * @return the requested result
   */
  List<DocumentActivity> findAllByDocumentIdOrderByOccurredAtAsc(Long documentId);
}
