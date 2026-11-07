package dev.roland.inventory_management_backend.features.document.dto;

import java.util.List;

/**
 * Totals and source balances calculated for a commercial draft preview.
 *
 * @param lines calculated source balances for each draft line
 * @param totals calculated document totals
 * @param approvalRequired whether the draft requires approval
 * @param valid whether the draft is valid
 */
public record CommercialPreviewResponse(
    List<CommercialPreviewLine> lines,
    DocumentTotals totals,
    boolean approvalRequired,
    boolean valid) {
  /** Copies line data to prevent callers from mutating the response after construction. */
  public CommercialPreviewResponse {
    lines = List.copyOf(lines);
  }
}
