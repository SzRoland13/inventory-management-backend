package dev.roland.inventory_management_backend.features.document.service;

import java.time.LocalDateTime;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.document.Document;

/** Defines operations supported by the inventory document feature. */
public interface DocumentPostingService {
  /**
   * Completes a document and posts stock movements for stock-affecting document types.
   *
   * @param document document to complete
   * @return completed document
   * @throws ApiException when a stock-affecting document does not define the required warehouse
   */
  Document complete(Document document);

  /**
   * Completes a document and applies its stock movements at the supplied effective time.
   *
   * @param document document to complete
   * @param effectiveAt optional effective movement time
   * @return completed document
   */
  Document complete(Document document, LocalDateTime effectiveAt);
}
