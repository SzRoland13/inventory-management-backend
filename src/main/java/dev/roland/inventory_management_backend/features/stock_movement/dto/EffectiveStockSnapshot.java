package dev.roland.inventory_management_backend.features.stock_movement.dto;

import java.math.BigDecimal;

/** Product quantity snapshot derived from effective stock movements. */
public interface EffectiveStockSnapshot {
  /**
   * Returns the product identifier.
   *
   * @return product identifier
   */
  Long getProductId();

  /**
   * Returns the effective quantity at the snapshot timestamp.
   *
   * @return effective quantity at the snapshot timestamp
   */
  BigDecimal getQuantity();
}
