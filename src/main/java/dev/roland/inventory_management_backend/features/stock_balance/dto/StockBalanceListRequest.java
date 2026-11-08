package dev.roland.inventory_management_backend.features.stock_balance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Paging and filters for the company stock balance list.
 *
 * @param page zero-based page number
 * @param size requested page size
 * @param search optional product SKU or name search
 * @param productId optional product filter
 * @param warehouseId optional warehouse filter
 */
public record StockBalanceListRequest(
    @Min(value = 0, message = ValidationKeys.INVALID_VALUE) Integer page,
    @Min(value = 1, message = ValidationKeys.INVALID_VALUE)
        @Max(value = 100, message = ValidationKeys.INVALID_VALUE)
        Integer size,
    @Size(max = 200, message = ValidationKeys.TOO_LONG) String search,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long productId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long warehouseId) {
  /**
   * Applies defaults for omitted paging values.
   *
   * @param page zero-based page number
   * @param size requested page size
   * @param search optional product SKU or name search
   * @param productId optional product filter
   * @param warehouseId optional warehouse filter
   */
  public StockBalanceListRequest {
    page = page == null ? Integer.valueOf(0) : page;
    size = size == null ? Integer.valueOf(25) : size;
  }
}
