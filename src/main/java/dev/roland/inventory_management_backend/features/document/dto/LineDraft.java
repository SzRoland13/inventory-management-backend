package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Defines the line draft document API contract.
 *
 * @param productId product identifier value
 * @param quantity quantity value
 * @param unitId unit identifier value
 * @param unitPrice unit price in document currency
 * @param sourceUnitPrice original unit price in the product currency
 * @param sourceCurrencyId source currency snapshot identifier
 */
public record LineDraft(
    @NotNull(message = ValidationKeys.REQUIRED) @Positive(message = ValidationKeys.INVALID_VALUE)
        Long productId,
    @NotNull(message = ValidationKeys.REQUIRED) @Positive(message = ValidationKeys.INVALID_VALUE)
        Long unitId,
    @NotNull(message = ValidationKeys.REQUIRED)
        @Positive(message = ValidationKeys.INVALID_VALUE)
        @Digits(integer = 16, fraction = 8, message = ValidationKeys.INVALID_VALUE)
        BigDecimal quantity,
    @DecimalMin(value = "0", message = ValidationKeys.INVALID_VALUE)
        @Digits(integer = 16, fraction = 8, message = ValidationKeys.INVALID_VALUE)
        BigDecimal unitPrice,
    @DecimalMin(value = "0", message = ValidationKeys.INVALID_VALUE)
        @Digits(integer = 16, fraction = 8, message = ValidationKeys.INVALID_VALUE)
        BigDecimal sourceUnitPrice,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long sourceCurrencyId) {
  /**
   * Creates a draft line with a source price and no currency snapshot.
   *
   * @param productId product identifier
   * @param unitId unit identifier
   * @param quantity line quantity
   * @param unitPrice unit price in document currency
   * @param sourceUnitPrice original unit price in the source currency
   */
  public LineDraft(
      final Long productId,
      final Long unitId,
      final BigDecimal quantity,
      final BigDecimal unitPrice,
      final BigDecimal sourceUnitPrice) {
    this(productId, unitId, quantity, unitPrice, sourceUnitPrice, null);
  }

  /**
   * Creates a draft line without a separate source price.
   *
   * @param productId product identifier
   * @param unitId unit identifier
   * @param quantity line quantity
   * @param unitPrice source unit price
   */
  public LineDraft(
      final Long productId,
      final Long unitId,
      final BigDecimal quantity,
      final BigDecimal unitPrice) {
    this(productId, unitId, quantity, unitPrice, null, null);
  }
}
