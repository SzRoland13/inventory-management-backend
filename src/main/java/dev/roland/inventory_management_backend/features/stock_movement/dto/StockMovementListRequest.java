package dev.roland.inventory_management_backend.features.stock_movement.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Paging and filters for the posted stock movement list.
 *
 * @param page zero-based page number
 * @param size requested page size
 * @param productId optional product filter
 * @param warehouseId optional warehouse filter
 * @param startDate inclusive start date
 * @param endDate inclusive end date
 */
public record StockMovementListRequest(
    @Min(value = 0, message = ValidationKeys.INVALID_VALUE) Integer page,
    @Min(value = 1, message = ValidationKeys.INVALID_VALUE)
        @Max(value = 100, message = ValidationKeys.INVALID_VALUE)
        Integer size,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long productId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long warehouseId,
    LocalDate startDate,
    LocalDate endDate) {
  /**
   * Applies defaults for omitted paging values.
   *
   * @param page zero-based page number
   * @param size requested page size
   * @param productId optional product filter
   * @param warehouseId optional warehouse filter
   * @param startDate inclusive start date
   * @param endDate inclusive end date
   */
  public StockMovementListRequest {
    page = page == null ? Integer.valueOf(0) : page;
    size = size == null ? Integer.valueOf(25) : size;
  }

  /** Rejects a range whose beginning is after its end. */
  @AssertTrue(message = ValidationKeys.INVALID_VALUE)
  public boolean isDateRangeValid() {
    return startDate == null || endDate == null || !startDate.isAfter(endDate);
  }
}
