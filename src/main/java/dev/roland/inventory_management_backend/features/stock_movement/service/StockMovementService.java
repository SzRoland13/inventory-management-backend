package dev.roland.inventory_management_backend.features.stock_movement.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementQueryRow;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

/** Defines operations supported by the stock movement feature. */
public interface StockMovementService extends BaseService<StockMovement, Long> {
  /** Searches posted movements within a company and optional filters. */
  Page<StockMovementQueryRow> findCompanyMovements(
      Long companyId,
      Long productId,
      Long warehouseId,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Pageable pageable);

  /**
   * Checks whether a matching record exists.
   *
   * @param productId the product identifier
   * @return true if a matching record exists
   */
  boolean existsByProductId(Long productId);

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
