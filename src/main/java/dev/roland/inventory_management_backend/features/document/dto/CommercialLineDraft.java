package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

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
    Long sourceLineId,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal vatRate,
    BigDecimal amount) {}
