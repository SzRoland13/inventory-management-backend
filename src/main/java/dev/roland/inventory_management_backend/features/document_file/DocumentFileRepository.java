package dev.roland.inventory_management_backend.features.document_file;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Defines the document file repository document API contract. */
public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {
  /**
   * Finds records matching the supplied criteria.
   *
   * @param documentId document identifier value
   * @return the requested result
   */
  List<DocumentFile> findAllByDocumentIdOrderByCreatedAtDesc(Long documentId);

  /**
   * Finds records matching the supplied criteria.
   *
   * @param documentId document identifier value
   * @param idempotencyKey idempotency key value
   * @return the requested result
   */
  Optional<DocumentFile> findByDocumentIdAndIdempotencyKey(Long documentId, String idempotencyKey);

  /**
   * Checks whether a matching record exists.
   *
   * @param documentId document identifier value
   * @param source source value
   * @param statuses statuses value
   * @return the requested result
   */
  boolean existsByDocumentIdAndSourceAndStatusIn(
      Long documentId, DocumentFileSource source, List<DocumentFileStatus> statuses);
}
