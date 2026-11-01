package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

/**
 * Defines the preview line document API contract.
 *
 * @param availableQuantity available quantity value
 * @param errorMessageKey error message key value
 * @param productId product identifier value
 * @param productName product name value
 * @param quantityDelta quantity delta value
 * @param resultingQuantity resulting quantity value
 * @param shortfall shortfall value
 * @param sourceLineId source line identifier value
 * @param warehouseName warehouse name value
 */
public record PreviewLine(
    Long sourceLineId,
    Long productId,
    String productName,
    String warehouseName,
    BigDecimal availableQuantity,
    BigDecimal quantityDelta,
    BigDecimal resultingQuantity,
    BigDecimal shortfall,
    String errorMessageKey) {}
