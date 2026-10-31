package dev.roland.inventory_management_backend.features.stock_movement.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class StockMovementListRequestTest {
  @Test
  void defaultsMissingPagingValuesAndPreservesSuppliedValues() {
    final var defaults = new StockMovementListRequest(null, null, null, null, null, null);
    final var supplied = new StockMovementListRequest(2, 50, null, null, null, null);

    assertEquals(0, defaults.page());
    assertEquals(25, defaults.size());
    assertEquals(2, supplied.page());
    assertEquals(50, supplied.size());
  }

  @Test
  void validatesOptionalAndOrderedDateRanges() {
    assertTrue(new StockMovementListRequest(0, 25, null, null, null, null).isDateRangeValid());
    assertTrue(
        new StockMovementListRequest(
                0, 25, null, null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1))
            .isDateRangeValid());
    assertFalse(
        new StockMovementListRequest(
                0, 25, null, null, LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 1))
            .isDateRangeValid());
  }
}
