package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Defines the fulfillment line document API contract.
 *
 * @param productId product identifier value
 * @param quantity quantity value
 * @param sourceLineId source line identifier value
 * @param unitId unit selected for the entered quantity
 */
public record FulfillmentLine(
    @NotNull(message = ValidationKeys.REQUIRED) @Positive(message = ValidationKeys.INVALID_VALUE)
        Long sourceLineId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long productId,
    @NotNull(message = ValidationKeys.REQUIRED)
        @Positive(message = ValidationKeys.INVALID_VALUE)
        @Digits(integer = 16, fraction = 8)
        BigDecimal quantity,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long unitId) {
  /** Keeps the existing fulfillment request constructor source-compatible. */
  public FulfillmentLine(final Long sourceLineId, final Long productId, final BigDecimal quantity) {
    this(sourceLineId, productId, quantity, null);
  }
}
