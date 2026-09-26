package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;

/** Stock quantity and optional box/remainder breakdown for one warehouse. */
public record ProductStockResponse(
    Long warehouseId,
    String warehouseName,
    Quantity stockQuantity,
    StockBreakdown displayQuantity) {

  /** A quantity paired with the unit used to express it. */
  public record Quantity(BigDecimal amount, UnitResponse unit) {}

  /** Stock expressed as full main units and an optional secondary-unit remainder. */
  public record StockBreakdown(Quantity fullMainUnits, Quantity secondaryUnitRemainder) {}
}
