package dev.roland.inventory_management_backend.features.stock_balance.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StockBalanceListRequestDefaultsTest {
  @Test
  void defaultsOmittedPageAndSize() {
    final StockBalanceListRequest request =
        new StockBalanceListRequest(null, null, null, null, null);

    assertEquals(0, request.page());
    assertEquals(25, request.size());
  }
}
