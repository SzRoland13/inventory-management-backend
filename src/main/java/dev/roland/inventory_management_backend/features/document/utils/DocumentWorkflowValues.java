package dev.roland.inventory_management_backend.features.document.utils;

import java.math.BigDecimal;
import java.util.List;

import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentCategory;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
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
   * @param value the value value
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
    return type == DocumentType.PURCHASE_ORDER || type == DocumentType.GOODS_RECEIPT
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
   */
  public static BigDecimal expectedFactor(final Product product, final Unit unit) {
    return product.getUnit() != null
            && product.getUnit().getId().equals(unit.getId())
            && product.getSecondaryUnit() != null
        ? product.getSecondaryUnitsPerMainUnit()
        : BigDecimal.ONE;
  }
}
