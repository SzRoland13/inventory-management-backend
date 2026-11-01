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
  public static BigDecimal nz(final BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  public static <T> List<T> safe(final List<T> values) {
    return values == null ? List.of() : values;
  }

  public static DocumentCategory category(final DocumentType type) {
    return type == DocumentType.PURCHASE_ORDER || type == DocumentType.GOODS_RECEIPT
        ? DocumentCategory.PROCUREMENT
        : DocumentCategory.SALES;
  }

  public static String number(final Document document) {
    return document.getDocumentNumber() == null
        ? document.getReferenceNumber() == null ? "" : document.getReferenceNumber()
        : document.getDocumentNumber();
  }

  public static Long idOf(final Warehouse warehouse) {
    return warehouse == null ? null : warehouse.getId();
  }

  public static BigDecimal expectedFactor(final Product product, final Unit unit) {
    return product.getUnit() != null
            && product.getUnit().getId().equals(unit.getId())
            && product.getSecondaryUnit() != null
        ? product.getSecondaryUnitsPerMainUnit()
        : BigDecimal.ONE;
  }
}
