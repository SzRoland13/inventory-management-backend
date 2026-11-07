package dev.roland.inventory_management_backend.features.document.dto;

import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableList;

import java.time.LocalDateTime;
import java.util.List;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;

/**
 * Defines the fulfillment preview document API contract.
 *
 * @param documentId document identifier value
 * @param lines lines value
 * @param sourceDocumentId source document identifier value
 * @param type type value
 * @param warehouseId warehouse identifier value
 * @param effectiveAt optional effective movement time
 */
public record FulfillmentPreview(
    Long sourceDocumentId,
    Long documentId,
    DocumentType type,
    Long warehouseId,
    LocalDateTime effectiveAt,
    List<FulfillmentLine> lines) {
  /** Keeps the existing request constructor source-compatible. */
  public FulfillmentPreview(
      final Long sourceDocumentId,
      final Long documentId,
      final DocumentType type,
      final Long warehouseId,
      final List<FulfillmentLine> lines) {
    this(sourceDocumentId, documentId, type, warehouseId, null, lines);
  }

  /**
   * Copies the line list to prevent external mutation.
   *
   * @param sourceDocumentId source document identifier
   * @param documentId fulfillment document identifier
   * @param type fulfillment document type
   * @param warehouseId warehouse identifier
   * @param lines fulfillment lines
   */
  public FulfillmentPreview {
    lines = immutableList(lines);
  }

  /**
   * Returns a defensive copy of the fulfillment lines.
   *
   * @return immutable fulfillment lines
   */
  @Override
  public List<FulfillmentLine> lines() {
    return immutableList(lines);
  }
}
