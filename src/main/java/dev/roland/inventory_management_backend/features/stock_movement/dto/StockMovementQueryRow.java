package dev.roland.inventory_management_backend.features.stock_movement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;

/** Interface projection containing a movement and its product unit metadata. */
public interface StockMovementQueryRow {
  /**
   * Returns the corresponding projected field.
   *
   * @return movement identifier
   */
  Long getId();

  /**
   * Returns the corresponding projected field.
   *
   * @return movement timestamp
   */
  LocalDateTime getDate();

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
   * @return quantity delta in main units
   */
  BigDecimal getQuantityChange();

  /**
   * Returns the corresponding projected field.
   *
   * @return movement type
   */
  StockMovementType getMovementType();

  /**
   * Returns the corresponding projected field.
   *
   * @return source document identifier, if any
   */
  Long getDocumentId();

  /**
   * Returns the corresponding projected field.
   *
   * @return source document number, if any
   */
  String getDocumentNumber();

  /**
   * Returns the corresponding projected field.
   *
   * @return source document line identifier, if any
   */
  Long getDocumentLineId();

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
