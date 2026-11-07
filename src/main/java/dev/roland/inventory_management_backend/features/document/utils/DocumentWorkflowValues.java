package dev.roland.inventory_management_backend.features.document.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentCategory;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import lombok.experimental.UtilityClass;

/** Pure document value helpers shared by workflow services and response mapping. */
@UtilityClass
public final class DocumentWorkflowValues {
  /**
   * Returns zero when the supplied quantity is null.
   *
   * @param value the value
   * @return the resulting value
   */
  public static BigDecimal nz(final BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  /**
   * Returns an empty list when the supplied list is null.
   *
   * @param <T> the <T> value
   * @param values the values value
   * @return the resulting value
   */
  public static <T> List<T> safe(final List<T> values) {
    return values == null ? List.of() : values;
  }

  /**
   * Determines the category associated with a document type.
   *
   * @param type the type value
   * @return the resulting value
   */
  public static DocumentCategory category(final DocumentType type) {
    return type == DocumentType.PURCHASE_ORDER
            || type == DocumentType.GOODS_RECEIPT
            || type == DocumentType.SUPPLIER_BILL
        ? DocumentCategory.PROCUREMENT
        : DocumentCategory.SALES;
  }

  /**
   * Returns the document number, falling back to its reference number.
   *
   * @param document the document value
   * @return the resulting value
   */
  public static String number(final Document document) {
    return document.getDocumentNumber() == null
        ? document.getReferenceNumber() == null ? "" : document.getReferenceNumber()
        : document.getDocumentNumber();
  }

  /**
   * Returns a warehouse identifier, or null when no warehouse is set.
   *
   * @param warehouse the warehouse value
   * @return the resulting value
   */
  public static Long idOf(final Warehouse warehouse) {
    return warehouse == null ? null : warehouse.getId();
  }

  /**
   * Returns the conversion factor expected for the supplied product unit.
   *
   * @param product the product value
   * @param unit the unit value
   * @return the resulting value
   * @throws ApiException when the unit is not configured on the product
   */
  public static BigDecimal expectedFactor(final Product product, final Unit unit) {
    final BigDecimal factor;
    if (product.getUnit() == null && unit == null) {
      factor = BigDecimal.ONE;
    } else if (product.getUnit() != null && product.getUnit().getId().equals(unit.getId())) {
      factor =
          product.getSecondaryUnit() == null
              ? BigDecimal.ONE
              : product.getSecondaryUnitsPerMainUnit();
    } else if (product.getSecondaryUnit() != null
        && product.getSecondaryUnit().getId().equals(unit.getId())) {
      factor = BigDecimal.ONE;
    } else {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    return factor;
  }

  /**
   * Resolves one of the units configured on a product.
   *
   * @param product product with configured units
   * @param unitId selected unit identifier, or null for the main unit
   * @return the configured unit
   * @throws ApiException when the identifier is not configured on the product
   */
  public static Unit configuredUnit(final Product product, final Long unitId) {
    final Unit unit;
    if (unitId == null || (product.getUnit() != null && unitId.equals(product.getUnit().getId()))) {
      unit = product.getUnit();
    } else if (product.getSecondaryUnit() != null
        && unitId.equals(product.getSecondaryUnit().getId())) {
      unit = product.getSecondaryUnit();
    } else {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    return unit;
  }

  /**
   * Returns the normalized stock quantity for a line entered in its snapshotted unit.
   *
   * @param line line carrying the conversion snapshot
   * @param quantity quantity entered in that line's unit
   * @return normalized quantity in stock units
   */
  public static BigDecimal stockQuantity(final DocumentLine line, final BigDecimal quantity) {
    return nz(quantity).multiply(factorSnapshot(line));
  }

  /**
   * Converts stock units into the supplied line's snapshotted entry unit.
   *
   * @param line line carrying the conversion snapshot
   * @param stockQuantity quantity in stock units
   * @return quantity in the line's unit
   */
  public static BigDecimal entryQuantity(final DocumentLine line, final BigDecimal stockQuantity) {
    return nz(stockQuantity).divide(factorSnapshot(line), 8, RoundingMode.HALF_UP);
  }

  /**
   * Resolves the factor for an operation quantity, preserving a source snapshot by default.
   *
   * @param source source line, or null for a new product line
   * @param selectedUnitId selected unit identifier, or null to preserve the source unit
   * @return stock units represented by one operation unit
   * @throws ApiException when the selected unit is not configured for the product
   */
  public static BigDecimal operationFactor(final DocumentLine source, final Long selectedUnitId) {
    final BigDecimal factor;
    if (source == null) {
      factor = BigDecimal.ONE;
    } else if (selectedUnitId == null) {
      factor = factorSnapshot(source);
    } else {
      final Product product = source.getProduct();
      if (source.getUnitSnapshot() != null
          && selectedUnitId.equals(source.getUnitSnapshot().getId())) {
        factor = factorSnapshot(source);
      } else if (product.getUnit() != null && selectedUnitId.equals(product.getUnit().getId())) {
        factor = expectedFactor(product, product.getUnit());
      } else if (product.getSecondaryUnit() != null
          && selectedUnitId.equals(product.getSecondaryUnit().getId())) {
        factor = BigDecimal.ONE;
      } else {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      }
    }
    return factor;
  }

  /**
   * Resolves a persisted line's conversion factor or its configured unit factor.
   *
   * @param line line to inspect
   * @return stock units represented by one line unit
   */
  private static BigDecimal factorSnapshot(final DocumentLine line) {
    final BigDecimal factor;

    if (line.getConversionFactorSnapshot() != null) {
      factor = line.getConversionFactorSnapshot();
    } else {
      final Unit unit =
          line.getUnitSnapshot() == null ? line.getProduct().getUnit() : line.getUnitSnapshot();
      factor = unit == null ? BigDecimal.ONE : expectedFactor(line.getProduct(), unit);
    }

    return factor;
  }

  /**
   * Resolves the configured unit for an operation and applies its quantity snapshot.
   *
   * @param target operation line to update
   * @param source source line used to resolve its product
   * @param selectedUnitId selected unit identifier
   * @throws ApiException when the selected unit is not configured for the product
   */
  public static void applyOperationUnit(
      final DocumentLine target, final DocumentLine source, final Long selectedUnitId) {

    if (source == null || selectedUnitId == null) {
      return;
    }

    final Product product = source.getProduct();
    final Unit unit;

    if (product.getUnit() != null && selectedUnitId.equals(product.getUnit().getId())) {
      unit = product.getUnit();
    } else if (product.getSecondaryUnit() != null
        && selectedUnitId.equals(product.getSecondaryUnit().getId())) {
      unit = product.getSecondaryUnit();
    } else {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }

    target.setUnitSnapshot(unit);
    target.setUnitCodeSnapshot(unit.getCode());
    target.setUnitNameSnapshot(unit.getName());
    target.setConversionFactorSnapshot(operationFactor(source, selectedUnitId));
  }
}
