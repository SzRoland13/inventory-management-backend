package dev.roland.inventory_management_backend.features.warehouse.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Message keys for warehouse lifecycle and validation results. */
@Getter
@RequiredArgsConstructor
public enum WarehouseMessageKey implements MessageKey {
  WAREHOUSES_RETRIEVED("warehouse.list.retrieved"),
  WAREHOUSE_CREATED("warehouse.created"),
  WAREHOUSE_UPDATED("warehouse.updated"),
  WAREHOUSE_ARCHIVED("warehouse.archived"),
  WAREHOUSE_RESTORED("warehouse.restored"),
  WAREHOUSE_NAME_ALREADY_EXISTS("warehouse.error.name_exists"),
  WAREHOUSE_CODE_ALREADY_EXISTS("warehouse.error.code_exists"),
  WAREHOUSE_HAS_NON_ZERO_BALANCE("warehouse.error.non_zero_balance"),
  WAREHOUSE_ALREADY_ARCHIVED("warehouse.error.already_archived"),
  WAREHOUSE_NOT_ARCHIVED("warehouse.error.not_archived"),
  WAREHOUSE_ARCHIVED_FOR_POSTING("warehouse.error.archived_for_posting"),
  WAREHOUSE_TYPE_CANNOT_CHANGE_WITH_STOCK("warehouse.error.type_cannot_change_with_stock"),
  INVALID_TRANSIT_DOCUMENT("warehouse.error.invalid_transit_document"),
  WAREHOUSE_PRODUCT_COMPANY_MISMATCH("warehouse.error.company_mismatch");

  private final String key;
}
