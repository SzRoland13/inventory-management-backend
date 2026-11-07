package dev.roland.inventory_management_backend.features.document.repository;

import java.util.Optional;

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

  /**
   * Finds a posting key for a document and idempotency token.
   *
   * @param documentId document identifier
   * @param idempotencyKey operation token
   * @return the posting key, if found
   */
  Optional<DocumentPostingKey> findByDocumentIdAndIdempotencyKey(
      Long documentId, String idempotencyKey);
}
