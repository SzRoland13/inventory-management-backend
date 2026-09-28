package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;

/**
 * Stock quantity and optional box/remainder breakdown for one warehouse.
 *
 * @param displayQuantity stock quantity broken down into display units
 * @param stockQuantity exact stored stock quantity and unit
 * @param warehouseId warehouse identifier
 * @param warehouseName warehouse name
 */
public record ProductStockResponse(
    Long warehouseId,
    String warehouseName,
    Quantity stockQuantity,
    StockBreakdown displayQuantity) {

  /**
   * A quantity paired with the unit used to express it.
   *
   * @param amount numeric quantity
   * @param unit unit used to express the quantity
   */
  public record Quantity(BigDecimal amount, UnitResponse unit) {}

  /**
   * Stock expressed as full main units and an optional secondary-unit remainder.
   *
   * @param fullMainUnits full main-unit quantity
   * @param secondaryUnitRemainder remaining secondary-unit quantity
   */
  public record StockBreakdown(Quantity fullMainUnits, Quantity secondaryUnitRemainder) {}
}
