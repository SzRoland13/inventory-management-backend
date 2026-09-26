package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;

/** Stock quantity and optional box/remainder breakdown for one warehouse. */
public record ProductStockResponse(
    Long warehouseId,
    String warehouseName,
    BigDecimal stockQuantity,
    String stockUnitCode,
    BigDecimal fullMainUnits,
    BigDecimal secondaryUnitRemainder) {}
