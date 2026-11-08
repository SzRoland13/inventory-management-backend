package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * One source-line allocation on a commercial document draft.
 *
 * @param sourceLineId source line identifier
 * @param quantity allocated quantity
 * @param unitPrice commercial unit price
 * @param vatRate commercial VAT rate
 * @param amount explicit net amount for amount-only credit allocations
 */
public record CommercialLineDraft(
    @Positive(message = ValidationKeys.INVALID_VALUE) Long sourceLineId,
    @Positive(message = ValidationKeys.INVALID_VALUE)
        @Digits(integer = 16, fraction = 8, message = ValidationKeys.INVALID_VALUE)
        BigDecimal quantity,
    @DecimalMin(value = "0", message = ValidationKeys.INVALID_VALUE)
        @Digits(integer = 16, fraction = 8, message = ValidationKeys.INVALID_VALUE)
        BigDecimal unitPrice,
    @DecimalMin(value = "0", message = ValidationKeys.INVALID_VALUE)
        @Digits(integer = 2, fraction = 3, message = ValidationKeys.INVALID_VALUE)
        BigDecimal vatRate,
    @DecimalMin(value = "0", message = ValidationKeys.INVALID_VALUE)
        @Digits(integer = 16, fraction = 8, message = ValidationKeys.INVALID_VALUE)
        BigDecimal amount) {}
