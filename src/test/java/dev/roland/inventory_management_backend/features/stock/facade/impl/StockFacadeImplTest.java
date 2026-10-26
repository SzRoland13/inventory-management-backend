package dev.roland.inventory_management_backend.features.stock.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceListRequest;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceQueryRow;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementListRequest;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementQueryRow;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;

@ExtendWith(MockitoExtension.class)
class StockFacadeImplTest {
  @Mock private StockBalanceService stockBalanceService;
  @Mock private StockMovementService stockMovementService;
  @Mock private CurrentCompanyProvider currentCompanyProvider;

  private StockFacadeImpl facade;

  @BeforeEach
  void setUp() {
    facade = new StockFacadeImpl(stockBalanceService, stockMovementService, currentCompanyProvider);
    when(currentCompanyProvider.getCurrentCompany()).thenReturn(Company.builder().id(42L).build());
  }

  @Test
  void mapsPagedCompanyBalancesAndReturnsQuantityInMainUnit() {
    final StockBalanceListRequest request = new StockBalanceListRequest(0, 25, "widget", null, 8L);
    final StockBalanceQueryRow row = balanceRow(null, new BigDecimal("12.50000000"));
    final Page<StockBalanceQueryRow> result =
        new PageImpl<>(List.of(row), PageRequest.of(0, 25), 1);
    when(stockBalanceService.findCompanyStockRows(42L, request, PageRequest.of(0, 25)))
        .thenReturn(result);

    final var response = facade.listBalances(request);

    assertEquals(1, response.content().size());
    assertEquals(new BigDecimal("12.50000000"), response.content().getFirst().quantity());
    assertEquals("EA", response.content().getFirst().mainUnit().code());
    assertNull(response.content().getFirst().secondaryUnit());
    assertEquals(1, response.page().totalElements());
    verify(stockBalanceService).findCompanyStockRows(42L, request, PageRequest.of(0, 25));
  }

  @Test
  void convertsStoredSecondaryBalanceToMainUnitAndIncludesSecondaryQuantity() {
    final StockBalanceListRequest request = new StockBalanceListRequest(0, 25, null, null, null);
    final StockBalanceQueryRow row = balanceRow(6L, new BigDecimal("30"));
    when(row.getSecondaryUnitCode()).thenReturn("BOX");
    when(row.getSecondaryUnitName()).thenReturn("Box");
    when(row.getSecondaryUnitSymbol()).thenReturn("box");
    when(row.isSecondaryUnitSystem()).thenReturn(false);
    when(row.getSecondaryUnitsPerMainUnit()).thenReturn(new BigDecimal("12"));
    when(stockBalanceService.findCompanyStockRows(42L, request, PageRequest.of(0, 25)))
        .thenReturn(new PageImpl<>(List.of(row), PageRequest.of(0, 25), 1));

    final var response = facade.listBalances(request);

    assertEquals(new BigDecimal("2.50000000"), response.content().getFirst().quantity());
    assertEquals(new BigDecimal("30"), response.content().getFirst().secondaryQuantity());
    assertEquals("BOX", response.content().getFirst().secondaryUnit().code());
  }

  private static StockBalanceQueryRow balanceRow(
      final Long secondaryUnitId, final BigDecimal quantity) {
    final StockBalanceQueryRow row = mock(StockBalanceQueryRow.class);
    when(row.getProductId()).thenReturn(3L);
    when(row.getProductSku()).thenReturn("W-3");
    when(row.getProductName()).thenReturn("Widget");
    when(row.getWarehouseId()).thenReturn(8L);
    when(row.getWarehouseName()).thenReturn("Main");
    when(row.getQuantity()).thenReturn(quantity);
    when(row.getMainUnitId()).thenReturn(5L);
    when(row.getMainUnitCode()).thenReturn("EA");
    when(row.getMainUnitName()).thenReturn("Each");
    when(row.getMainUnitSymbol()).thenReturn("ea");
    when(row.isMainUnitSystem()).thenReturn(true);
    when(row.getSecondaryUnitId()).thenReturn(secondaryUnitId);
    when(row.getSecondaryUnitsPerMainUnit())
        .thenReturn(secondaryUnitId == null ? null : BigDecimal.ONE);
    return row;
  }

  @Test
  void appliesInclusiveCalendarDateRangeAndPreservesSignedMovement() {
    final StockMovementListRequest request =
        new StockMovementListRequest(
            1, 10, 3L, 8L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2));
    final StockMovementQueryRow movement = mock(StockMovementQueryRow.class);
    when(movement.getId()).thenReturn(12L);
    when(movement.getDate()).thenReturn(LocalDateTime.of(2026, 10, 2, 23, 59));
    when(movement.getProductId()).thenReturn(3L);
    when(movement.getProductSku()).thenReturn("W-3");
    when(movement.getProductName()).thenReturn("Widget");
    when(movement.getWarehouseId()).thenReturn(8L);
    when(movement.getWarehouseName()).thenReturn("Main");
    when(movement.getQuantityChange()).thenReturn(new BigDecimal("-30"));
    when(movement.getMovementType()).thenReturn(StockMovementType.SALE_OUTBOUND);
    when(movement.getDocumentId()).thenReturn(25L);
    when(movement.getDocumentNumber()).thenReturn("INV-25");
    when(movement.getDocumentLineId()).thenReturn(31L);
    when(movement.getMainUnitId()).thenReturn(5L);
    when(movement.getMainUnitCode()).thenReturn("EA");
    when(movement.getMainUnitName()).thenReturn("Each");
    when(movement.getMainUnitSymbol()).thenReturn("ea");
    when(movement.isMainUnitSystem()).thenReturn(true);
    when(movement.getSecondaryUnitId()).thenReturn(6L);
    when(movement.getSecondaryUnitCode()).thenReturn("BOX");
    when(movement.getSecondaryUnitName()).thenReturn("Box");
    when(movement.getSecondaryUnitSymbol()).thenReturn("box");
    when(movement.isSecondaryUnitSystem()).thenReturn(false);
    when(movement.getSecondaryUnitsPerMainUnit()).thenReturn(new BigDecimal("12"));
    final Page<StockMovementQueryRow> result =
        new PageImpl<>(List.of(movement), PageRequest.of(1, 10), 1);
    when(stockMovementService.findCompanyMovements(
            eq(42L),
            eq(3L),
            eq(8L),
            eq(LocalDateTime.of(2026, 10, 1, 0, 0)),
            eq(LocalDateTime.of(2026, 10, 3, 0, 0)),
            org.mockito.ArgumentMatchers.argThat(
                (Pageable pageable) ->
                    pageable.getPageNumber() == 1 && pageable.getPageSize() == 10)))
        .thenReturn(result);

    final var response = facade.listMovements(request);

    assertEquals(new BigDecimal("-2.50000000"), response.content().getFirst().quantityChange());
    assertEquals(new BigDecimal("-30"), response.content().getFirst().secondaryQuantityChange());
    assertEquals(
        new UnitResponse(5L, "EA", "Each", "ea", true), response.content().getFirst().unit());
    assertEquals(25L, response.content().getFirst().documentId());
    assertEquals(31L, response.content().getFirst().documentLineId());
    verify(stockMovementService)
        .findCompanyMovements(
            eq(42L),
            eq(3L),
            eq(8L),
            eq(LocalDateTime.of(2026, 10, 1, 0, 0)),
            eq(LocalDateTime.of(2026, 10, 3, 0, 0)),
            org.mockito.ArgumentMatchers.argThat(
                pageable -> pageable.getPageNumber() == 1 && pageable.getPageSize() == 10));
  }
}
