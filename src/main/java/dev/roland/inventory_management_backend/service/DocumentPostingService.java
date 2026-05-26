package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.Document;

public interface DocumentPostingService {
  /**
   * Completes a document and posts stock movements for stock-affecting document types.
   *
   * @param document document to complete
   * @return completed document
   * @throws IllegalStateException when a stock-affecting document does not define the required
   *     warehouse
   */
  Document complete(Document document);
}
