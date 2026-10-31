package dev.roland.inventory_management_backend.features.document.dto;

import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableList;

import java.time.LocalDate;
import java.util.List;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;

/**
 * Defines the draft document API contract.
 *
 * @param date date value
 * @param lines lines value
 * @param notes notes value
 * @param partnerId partner identifier value
 * @param type type value
 * @param warehouseId warehouse identifier value
 */
public record Draft(
    DocumentType type,
    LocalDate date,
    Long partnerId,
    Long warehouseId,
    String notes,
    List<LineDraft> lines) {
  /**
   * Copies the line list to prevent external mutation.
   *
   * @param type document type
   * @param date document date
   * @param partnerId partner identifier
   * @param warehouseId warehouse identifier
   * @param notes document notes
   * @param lines source line list
   */
  public Draft {
    lines = immutableList(lines);
  }

  /**
   * Returns a defensive copy of the draft lines.
   *
   * @return immutable draft lines
   */
  @Override
  public List<LineDraft> lines() {
    return immutableList(lines);
  }
}
