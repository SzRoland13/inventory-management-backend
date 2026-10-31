package dev.roland.inventory_management_backend.features.stock_balance.dto;

import java.math.BigDecimal;

/** Interface projection for a company stock balance row. */
public interface StockBalanceQueryRow {
  /**
   * Returns the corresponding projected field.
   *
   * @return product identifier
   */
  Long getProductId();

  /**
   * Returns the corresponding projected field.
   *
   * @return product SKU
   */
  String getProductSku();

  /**
   * Returns the corresponding projected field.
   *
   * @return product display name
   */
  String getProductName();

  /**
   * Returns the corresponding projected field.
   *
   * @return warehouse identifier
   */
  Long getWarehouseId();

  /**
   * Returns the corresponding projected field.
   *
   * @return warehouse display name
   */
  String getWarehouseName();

  /**
   * Returns the corresponding projected field.
   *
   * @return on-hand quantity
   */
  BigDecimal getQuantity();

  /**
   * Returns the corresponding projected field.
   *
   * @return main unit identifier
   */
  Long getMainUnitId();

  /**
   * Returns the corresponding projected field.
   *
   * @return main unit code
   */
  String getMainUnitCode();

  /**
   * Returns the corresponding projected field.
   *
   * @return main unit display name
   */
  String getMainUnitName();

  /**
   * Returns the corresponding projected field.
   *
   * @return main unit symbol
   */
  String getMainUnitSymbol();

  /**
   * Returns the corresponding projected field.
   *
   * @return whether the main unit is a system unit
   */
  boolean isMainUnitSystem();

  /**
   * Returns the corresponding projected field.
   *
   * @return secondary unit identifier, if configured
   */
  Long getSecondaryUnitId();

  /**
   * Returns the corresponding projected field.
   *
   * @return secondary unit code, if configured
   */
  String getSecondaryUnitCode();

  /**
   * Returns the corresponding projected field.
   *
   * @return secondary unit display name, if configured
   */
  String getSecondaryUnitName();

  /**
   * Returns the corresponding projected field.
   *
   * @return secondary unit symbol, if configured
   */
  String getSecondaryUnitSymbol();

  /**
   * Returns the corresponding projected field.
   *
   * @return whether the secondary unit is a system unit
   */
  boolean isSecondaryUnitSystem();

  /**
   * Returns the corresponding projected field.
   *
   * @return number of secondary units per main unit
   */
  BigDecimal getSecondaryUnitsPerMainUnit();
}
