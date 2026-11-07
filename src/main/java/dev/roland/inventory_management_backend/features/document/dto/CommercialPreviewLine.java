package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

/**
 * Source balances and requested amount for one line in a commercial preview.
 *
 * @param sourceLineId identifier of the source document line, if any
 * @param eligibleQuantity source quantity eligible for this document
 * @param billedQuantity quantity already billed from the source line
 * @param remainingQuantity source quantity remaining after billing
 * @param creditedQuantity quantity already credited from the source line
 * @param remainingCreditableQuantity quantity still available for credit
 * @param creditedNetAmount net amount already credited from the source line
 * @param remainingCreditableNetAmount net amount still available for credit
 * @param requestedQuantity quantity requested by the draft
 * @param requestedNetAmount net amount requested by the draft
 */
public record CommercialPreviewLine(
    Long sourceLineId,
    BigDecimal eligibleQuantity,
    BigDecimal billedQuantity,
    BigDecimal remainingQuantity,
    BigDecimal creditedQuantity,
    BigDecimal remainingCreditableQuantity,
    BigDecimal creditedNetAmount,
    BigDecimal remainingCreditableNetAmount,
    BigDecimal requestedQuantity,
    BigDecimal requestedNetAmount) {}
