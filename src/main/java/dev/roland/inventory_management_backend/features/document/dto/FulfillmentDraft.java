package dev.roland.inventory_management_backend.features.document.dto;

import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableList;

import java.util.List;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;

/**
 * Defines the fulfillment draft document API contract.
 *
 * @param lines lines value
 * @param sourceDocumentId source document identifier value
 * @param type type value
 * @param warehouseId warehouse identifier value
 */
public record FulfillmentDraft(
    Long sourceDocumentId, DocumentType type, Long warehouseId, List<FulfillmentLine> lines) {
  /**
   * Copies the line list to prevent external mutation.
   *
   * @param sourceDocumentId source document identifier
   * @param type fulfillment document type
   * @param warehouseId warehouse identifier
   * @param lines fulfillment lines
   */
  public FulfillmentDraft {
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
