package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

/**
 * Defines the fulfillment line document API contract.
 *
 * @param productId product identifier value
 * @param quantity quantity value
 * @param sourceLineId source line identifier value
 * @param unitId unit selected for the entered quantity
 */
public record FulfillmentLine(Long sourceLineId, Long productId, BigDecimal quantity, Long unitId) {
  /** Keeps the existing fulfillment request constructor source-compatible. */
  public FulfillmentLine(final Long sourceLineId, final Long productId, final BigDecimal quantity) {
    this(sourceLineId, productId, quantity, null);
  }
}
