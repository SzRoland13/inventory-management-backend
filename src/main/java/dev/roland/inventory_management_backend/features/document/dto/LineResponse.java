package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

/**
 * Defines the line response document API contract.
 *
 * @param closedQuantity closed quantity value
 * @param closureReason closure reason value
 * @param fulfilledQuantity fulfilled quantity value
 * @param id id value
 * @param operationQuantity operation quantity value
 * @param orderedQuantity ordered quantity value
 * @param productCode product code value
 * @param productId product identifier value
 * @param productName product name value
 * @param remainingQuantity remaining quantity value
 * @param sourceLineId source line identifier value
 * @param unitName unit name value
 * @param unitPrice unit price in document currency
 * @param sourceUnitPrice original unit price in the product currency
 */
public record LineResponse(
    Long id,
    Long productId,
    String productName,
    String productCode,
    String unitName,
    BigDecimal orderedQuantity,
    BigDecimal fulfilledQuantity,
    BigDecimal closedQuantity,
    String closureReason,
    BigDecimal remainingQuantity,
    BigDecimal operationQuantity,
    BigDecimal unitPrice,
    BigDecimal sourceUnitPrice,
    Long sourceLineId) {}
