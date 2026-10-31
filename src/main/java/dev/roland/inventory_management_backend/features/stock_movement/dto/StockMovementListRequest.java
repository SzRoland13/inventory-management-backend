package dev.roland.inventory_management_backend.features.stock_movement.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

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
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size,
    @Positive Long productId,
    @Positive Long warehouseId,
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
  @AssertTrue(message = "Start date must not be after end date")
  public boolean isDateRangeValid() {
    return startDate == null || endDate == null || !startDate.isAfter(endDate);
  }
}
