package dev.roland.inventory_management_backend.features.stock_balance.dto;

import java.math.BigDecimal;

import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;

/**
 * Company stock balance for one product and warehouse.
 *
 * @param productId product identifier
 * @param productSku product SKU
 * @param productName product display name
 * @param warehouseId warehouse identifier
 * @param warehouseName warehouse display name
 * @param quantity quantity expressed in main units
 * @param mainUnit product main unit
 * @param secondaryQuantity quantity expressed in secondary units, if configured
 * @param secondaryUnit product secondary unit, if configured
 * @param secondaryUnitsPerMainUnit conversion factor, if configured
 */
public record StockBalanceResponse(
    Long productId,
    String productSku,
    String productName,
    Long warehouseId,
    String warehouseName,
    BigDecimal quantity,
    UnitResponse mainUnit,
    BigDecimal secondaryQuantity,
    UnitResponse secondaryUnit,
    BigDecimal secondaryUnitsPerMainUnit) {}
