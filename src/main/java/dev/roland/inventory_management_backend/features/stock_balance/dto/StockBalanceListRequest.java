package dev.roland.inventory_management_backend.features.stock_balance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Paging and filters for the company stock balance list. */
public record StockBalanceListRequest(
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size,
    @Size(max = 200) String search,
    @Positive Long productId,
    @Positive Long warehouseId) {
  public StockBalanceListRequest {
    page = page == null ? 0 : page;
    size = size == null ? 25 : size;
  }
}
