package dev.roland.inventory_management_backend.features.stock.facade;

import dev.roland.inventory_management_backend.common.dto.PageResponse;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceListRequest;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceResponse;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementListRequest;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementResponse;

/** Coordinates company-scoped stock read operations. */
public interface StockFacade {
  /**
   * Lists current company stock balances.
   *
   * @param request paging and filter values
   * @return requested page of balances
   */
  PageResponse<StockBalanceResponse> listBalances(StockBalanceListRequest request);

  /**
   * Lists posted current company stock movements.
   *
   * @param request paging and filter values
   * @return requested page of movements
   */
  PageResponse<StockMovementResponse> listMovements(StockMovementListRequest request);
}
