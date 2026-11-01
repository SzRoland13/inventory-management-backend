package dev.roland.inventory_management_backend.features.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.document.DocumentPostingKey;

/** Stores keys that make document posting retries idempotent. */
public interface DocumentPostingKeyRepository extends JpaRepository<DocumentPostingKey, Long> {
  /**
   * Checks whether a matching record exists.
   *
   * @param documentId document identifier value
   * @param idempotencyKey idempotency key value
   * @return the requested result
   */
  boolean existsByDocumentIdAndIdempotencyKey(Long documentId, String idempotencyKey);
}
