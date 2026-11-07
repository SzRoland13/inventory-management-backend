package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;

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
 * @param sourceCurrencyId source currency snapshot identifier
 * @param countedQuantity the countedQuantity value
 * @param expectedQuantity the expectedQuantity value
 * @param inTransitQuantity the inTransitQuantity value
 * @param returnableQuantity the returnableQuantity value
 * @param returnedQuantity the returnedQuantity value
 * @param variance the variance value
 * @param unitId selected unit identifier
 * @param conversionFactorSnapshot factor converting this line to stock units
 * @param mainUnit configured main unit
 * @param secondaryUnit configured secondary unit, if present
 * @param secondaryUnitsPerMainUnit configured conversion ratio
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
    Long sourceCurrencyId,
    Long sourceLineId,
    BigDecimal returnedQuantity,
    BigDecimal returnableQuantity,
    BigDecimal inTransitQuantity,
    BigDecimal expectedQuantity,
    BigDecimal countedQuantity,
    BigDecimal variance,
    Long unitId,
    BigDecimal conversionFactorSnapshot,
    UnitResponse mainUnit,
    UnitResponse secondaryUnit,
    BigDecimal secondaryUnitsPerMainUnit) {}
