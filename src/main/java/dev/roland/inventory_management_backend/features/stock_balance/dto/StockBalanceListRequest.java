package dev.roland.inventory_management_backend.features.stock_balance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size,
    @Size(max = 200) String search,
    @Positive Long productId,
    @Positive Long warehouseId) {
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
