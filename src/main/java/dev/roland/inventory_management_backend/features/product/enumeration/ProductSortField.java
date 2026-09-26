package dev.roland.inventory_management_backend.features.product.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Product fields allowed as a catalog sort key. */
@Getter
@RequiredArgsConstructor
public enum ProductSortField {
  SKU("sku"),
  NAME("name"),
  BRAND("brand"),
  STATUS("status"),
  NET_PRICE("netPrice"),
  VAT_RATE("vatRate"),
  CREATED_AT("createdAt"),
  UPDATED_AT("updatedAt");

  private final String property;
}
