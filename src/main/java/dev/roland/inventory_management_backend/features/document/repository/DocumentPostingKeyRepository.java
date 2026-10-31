package dev.roland.inventory_management_backend.features.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.document.DocumentPostingKey;

/** Stores keys that make document posting retries idempotent. */
public interface DocumentPostingKeyRepository extends JpaRepository<DocumentPostingKey, Long> {
  boolean existsByDocumentIdAndIdempotencyKey(Long documentId, String idempotencyKey);
}
