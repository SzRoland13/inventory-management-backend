package dev.roland.inventory_management_backend.features.document.dto;

import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableList;

import java.util.List;

/**
 * Defines the preview response document API contract.
 *
 * @param lines lines value
 * @param valid valid value
 */
public record PreviewResponse(boolean valid, List<PreviewLine> lines) {
  /**
   * Copies the line list to prevent external mutation.
   *
   * @param valid whether the preview is valid
   * @param lines preview lines
   */
  public PreviewResponse {
    lines = immutableList(lines);
  }

  /**
   * Returns a defensive copy of preview lines.
   *
   * @return immutable preview lines
   */
  @Override
  public List<PreviewLine> lines() {
    return immutableList(lines);
  }
}
