package dev.roland.inventory_management_backend.features.stock.facade.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.dto.PageResponse;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.stock.facade.StockFacade;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceListRequest;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceQueryRow;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceResponse;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementListRequest;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementQueryRow;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementResponse;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import lombok.RequiredArgsConstructor;

/** Coordinates read-only stock queries and maps their paginated results. */
@Service
@RequiredArgsConstructor
public class StockFacadeImpl implements StockFacade {
  private final StockBalanceService stockBalanceService;
  private final StockMovementService stockMovementService;
  private final CurrentCompanyProvider currentCompanyProvider;

  @Override
  public PageResponse<StockBalanceResponse> listBalances(final StockBalanceListRequest request) {
    final var pageable = PageRequest.of(request.page(), request.size());
    final Page<StockBalanceQueryRow> rows =
        stockBalanceService.findCompanyStockRows(
            currentCompanyProvider.getCurrentCompany().getId(), request, pageable);
    return PageResponse.from(rows.map(StockFacadeImpl::toBalanceResponse));
  }

  @Override
  public PageResponse<StockMovementResponse> listMovements(final StockMovementListRequest request) {
    final LocalDateTime start =
        request.startDate() == null ? null : request.startDate().atStartOfDay();
    final LocalDateTime endExclusive =
        request.endDate() == null ? null : request.endDate().plusDays(1).atStartOfDay();
    final Page<StockMovementQueryRow> movements =
        stockMovementService.findCompanyMovements(
            currentCompanyProvider.getCurrentCompany().getId(),
            request.productId(),
            request.warehouseId(),
            start,
            endExclusive,
            PageRequest.of(request.page(), request.size()));
    return PageResponse.from(movements.map(StockFacadeImpl::toMovementResponse));
  }

  private static StockBalanceResponse toBalanceResponse(final StockBalanceQueryRow row) {
    final UnitResponse mainUnit =
        new UnitResponse(
            row.getMainUnitId(),
            row.getMainUnitCode(),
            row.getMainUnitName(),
            row.getMainUnitSymbol(),
            row.isMainUnitSystem());
    final UnitResponse secondaryUnit =
        row.getSecondaryUnitId() == null
            ? null
            : new UnitResponse(
                row.getSecondaryUnitId(),
                row.getSecondaryUnitCode(),
                row.getSecondaryUnitName(),
                row.getSecondaryUnitSymbol(),
                row.isSecondaryUnitSystem());
    return new StockBalanceResponse(
        row.getProductId(),
        row.getProductSku(),
        row.getProductName(),
        row.getWarehouseId(),
        row.getWarehouseName(),
        toMainQuantity(
            row.getQuantity(), row.getSecondaryUnitId(), row.getSecondaryUnitsPerMainUnit()),
        mainUnit,
        row.getSecondaryUnitId() == null ? null : row.getQuantity(),
        secondaryUnit,
        row.getSecondaryUnitsPerMainUnit());
  }

  private static StockMovementResponse toMovementResponse(final StockMovementQueryRow row) {
    final UnitResponse mainUnit =
        new UnitResponse(
            row.getMainUnitId(),
            row.getMainUnitCode(),
            row.getMainUnitName(),
            row.getMainUnitSymbol(),
            row.isMainUnitSystem());
    final UnitResponse secondaryUnit =
        row.getSecondaryUnitId() == null
            ? null
            : new UnitResponse(
                row.getSecondaryUnitId(),
                row.getSecondaryUnitCode(),
                row.getSecondaryUnitName(),
                row.getSecondaryUnitSymbol(),
                row.isSecondaryUnitSystem());
    return new StockMovementResponse(
        row.getId(),
        row.getDate(),
        row.getProductId(),
        row.getProductSku(),
        row.getProductName(),
        row.getWarehouseId(),
        row.getWarehouseName(),
        toMainQuantity(
            row.getQuantityChange(), row.getSecondaryUnitId(), row.getSecondaryUnitsPerMainUnit()),
        mainUnit,
        row.getSecondaryUnitId() == null ? null : row.getQuantityChange(),
        secondaryUnit,
        row.getMovementType(),
        row.getDocumentId(),
        row.getDocumentNumber(),
        row.getDocumentLineId());
  }

  private static BigDecimal toMainQuantity(
      final BigDecimal quantity, final Long secondaryUnitId, final BigDecimal factor) {
    return secondaryUnitId == null ? quantity : quantity.divide(factor, 8, RoundingMode.HALF_UP);
  }
}
