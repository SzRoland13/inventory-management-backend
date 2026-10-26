package dev.roland.inventory_management_backend.features.stock_movement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;

/** Interface projection containing a movement and its product unit metadata. */
public interface StockMovementQueryRow {
  Long getId();

  LocalDateTime getDate();

  Long getProductId();

  String getProductSku();

  String getProductName();

  Long getWarehouseId();

  String getWarehouseName();

  BigDecimal getQuantityChange();

  StockMovementType getMovementType();

  Long getDocumentId();

  String getDocumentNumber();

  Long getDocumentLineId();

  Long getMainUnitId();

  String getMainUnitCode();

  String getMainUnitName();

  String getMainUnitSymbol();

  boolean isMainUnitSystem();

  Long getSecondaryUnitId();

  String getSecondaryUnitCode();

  String getSecondaryUnitName();

  String getSecondaryUnitSymbol();

  boolean isSecondaryUnitSystem();

  BigDecimal getSecondaryUnitsPerMainUnit();
}
