package dev.roland.inventory_management_backend.features.document.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.DocumentPostingKey;

/** Defines operations supported by the inventory document feature. */
public interface DocumentService extends BaseService<Document, Long> {
  /**
   * Finds a document owned by the specified company.
   *
   * @param id document identifier
   * @param companyId owning company identifier
   * @return matching document, if found
   */
  Optional<Document> findByIdAndCompanyId(Long id, Long companyId);

  /**
   * Locks a document owned by the specified company for workflow updates.
   *
   * @param id document identifier
   * @param companyId owning company identifier
   * @return locked document, if found
   */
  Optional<Document> lockByIdAndCompanyId(Long id, Long companyId);

  /**
   * Lists company documents in most recently updated order.
   *
   * @param companyId owning company identifier
   * @return documents ordered by update time
   */
  List<Document> findAllByCompanyIdOrderByUpdatedAtDesc(Long companyId);

  /** Flushes pending document changes before constructing the response. */
  void flush();

  /**
   * Checks whether the document has already consumed the supplied posting key.
   *
   * @param documentId document identifier
   * @param key posting idempotency key
   * @return whether the key exists for the document
   */
  boolean postingKeyExists(Long documentId, String key);

  /**
   * Finds a consumed posting key and its resulting document.
   *
   * @param documentId document identifier
   * @param key posting idempotency key
   * @return the key, if found
   */
  Optional<DocumentPostingKey> findPostingKey(Long documentId, String key);

  /**
   * Persists a posting idempotency key for the document.
   *
   * @param key posting key to persist
   * @return persisted posting key
   */
  DocumentPostingKey savePostingKey(DocumentPostingKey key);
}
