package dev.roland.inventory_management_backend.features.stock_balance.dto;

import java.math.BigDecimal;

/** Interface projection for a company stock balance row. */
public interface StockBalanceQueryRow {
  Long getProductId();

  String getProductSku();

  String getProductName();

  Long getWarehouseId();

  String getWarehouseName();

  BigDecimal getQuantity();

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
