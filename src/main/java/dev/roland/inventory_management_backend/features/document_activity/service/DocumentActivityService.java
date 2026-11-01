package dev.roland.inventory_management_backend.features.document_activity.service;

import java.util.List;

import dev.roland.inventory_management_backend.features.document_activity.DocumentActivity;

/** Owns persistence operations for document activity events. */
public interface DocumentActivityService {
  /**
   * Persists an activity event.
   *
   * @param activity event to persist
   * @return persisted event
   */
  DocumentActivity save(DocumentActivity activity);

  /**
   * Lists a document's activity in occurrence order.
   *
   * @param documentId document identifier
   * @return events ordered by occurrence time
   */
  List<DocumentActivity> findAllByDocumentIdOrderByOccurredAtAsc(Long documentId);
}
