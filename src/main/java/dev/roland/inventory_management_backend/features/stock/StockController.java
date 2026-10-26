package dev.roland.inventory_management_backend.features.stock;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.common.annotation.AdminOrManager;
import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.common.dto.PageResponse;
import dev.roland.inventory_management_backend.features.stock.facade.StockFacade;
import dev.roland.inventory_management_backend.features.stock.message.StockMessageKey;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceListRequest;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceResponse;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementListRequest;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementResponse;
import lombok.RequiredArgsConstructor;

/** Exposes read-only company stock balances and movement history. */
@RestController
@RequestMapping(StockController.BASE_PATH)
@RequiredArgsConstructor
@AdminOrManager
public class StockController {
  public static final String BASE_PATH = "api/v1/stocks";
  public static final String BALANCES_PATH = "/balances";
  public static final String MOVEMENTS_PATH = "/movements";

  private final StockFacade stockFacade;

  /** Lists current on-hand quantities. */
  @GetMapping(BALANCES_PATH)
  public ResponseEntity<ApiResponse<PageResponse<StockBalanceResponse>>> listBalances(
      @Valid @ModelAttribute final StockBalanceListRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(StockMessageKey.BALANCES_RETRIEVED, stockFacade.listBalances(request)));
  }

  /** Lists posted stock ledger entries. */
  @GetMapping(MOVEMENTS_PATH)
  public ResponseEntity<ApiResponse<PageResponse<StockMovementResponse>>> listMovements(
      @Valid @ModelAttribute final StockMovementListRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            StockMessageKey.MOVEMENTS_RETRIEVED, stockFacade.listMovements(request)));
  }
}
