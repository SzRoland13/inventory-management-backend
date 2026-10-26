package dev.roland.inventory_management_backend.features.stock_balance.dto;

import java.math.BigDecimal;

import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;

/** Company stock balance for one product and warehouse. */
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
