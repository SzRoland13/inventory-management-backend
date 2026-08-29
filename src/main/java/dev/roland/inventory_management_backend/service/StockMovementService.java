package dev.roland.inventory_management_backend.service;

import java.math.BigDecimal;

import dev.roland.inventory_management_backend.enums.StockMovementType;
import dev.roland.inventory_management_backend.model.DocumentLine;
import dev.roland.inventory_management_backend.model.StockMovement;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.model.Warehouse;

public interface StockMovementService extends BaseService<StockMovement, Long> {
  /**
   * Creates an immutable stock movement entry and applies its quantity change to the stock balance.
   *
   * @param documentLine source document line for the movement
   * @param warehouse warehouse whose balance is affected
   * @param quantityChange signed quantity delta to apply
   * @param movementType business reason for the movement
   * @param createdByUser user responsible for creating the movement, or null for system actions
   * @return persisted stock movement
   */
  StockMovement postMovement(
      DocumentLine documentLine,
      Warehouse warehouse,
      BigDecimal quantityChange,
      StockMovementType movementType,
      User createdByUser);
}
