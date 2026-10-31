package dev.roland.inventory_management_backend.features.stock_movement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;

/**
 * Read-only stock ledger entry.
 *
 * @param id movement identifier
 * @param date movement timestamp
 * @param productId product identifier
 * @param productSku product SKU
 * @param productName product display name
 * @param warehouseId warehouse identifier
 * @param warehouseName warehouse display name
 * @param quantityChange signed quantity delta in main units
 * @param unit product main unit
 * @param secondaryQuantityChange signed quantity delta in secondary units, if configured
 * @param secondaryUnit product secondary unit, if configured
 * @param movementType movement classification
 * @param documentId source document identifier, if any
 * @param documentNumber source document number, if any
 * @param documentLineId source document line identifier, if any
 */
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
