package dev.roland.inventory_management_backend.features.document_file.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.features.document_file.DocumentFile;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileSource;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileStatus;

/** Owns persistence operations for document files and PDF attempts. */
public interface DocumentFileService {
  /**
   * Persists an uploaded file or generated PDF attempt.
   *
   * @param file file record to persist
   * @return persisted file record
   */
  DocumentFile save(DocumentFile file);

  /**
   * Lists document files from newest to oldest.
   *
   * @param documentId document identifier
   * @return files ordered by creation time
   */
  List<DocumentFile> findAllByDocumentIdOrderByCreatedAtDesc(Long documentId);

  /**
   * Finds a file attempt by document and idempotency key.
   *
   * @param documentId document identifier
   * @param key idempotency key
   * @return matching file attempt, if found
   */
  Optional<DocumentFile> findByDocumentIdAndIdempotencyKey(Long documentId, String key);

  /**
   * Checks whether a file attempt has one of the supplied statuses.
   *
   * @param documentId document identifier
   * @param source file source
   * @param statuses statuses to match
   * @return whether a matching file exists
   */
  boolean existsByDocumentIdAndSourceAndStatusIn(
      Long documentId, DocumentFileSource source, List<DocumentFileStatus> statuses);
}
