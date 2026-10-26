package dev.roland.inventory_management_backend.features.stock_movement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;

/** Read-only stock ledger entry. */
public record StockMovementResponse(
    Long id,
    LocalDateTime date,
    Long productId,
    String productSku,
    String productName,
    Long warehouseId,
    String warehouseName,
    BigDecimal quantityChange,
    UnitResponse unit,
    BigDecimal secondaryQuantityChange,
    UnitResponse secondaryUnit,
    StockMovementType movementType,
    Long documentId,
    String documentNumber,
    Long documentLineId) {}
