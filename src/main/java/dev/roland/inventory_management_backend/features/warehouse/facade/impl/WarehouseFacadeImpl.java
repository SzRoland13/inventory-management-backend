package dev.roland.inventory_management_backend.features.warehouse.facade.impl;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseRequest;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseResponse;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import dev.roland.inventory_management_backend.features.warehouse.facade.WarehouseFacade;
import dev.roland.inventory_management_backend.features.warehouse.message.WarehouseMessageKey;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;

/** Coordinates warehouse operations with the active company and stock balances. */
@Service
@RequiredArgsConstructor
public class WarehouseFacadeImpl implements WarehouseFacade {
  private final WarehouseService warehouseService;
  private final CurrentCompanyProvider currentCompanyProvider;
  private final StockBalanceService stockBalanceService;

  @Override
  public List<WarehouseResponse> findAllForCurrentCompany() {
    return warehouseService.findAllByCompanyId(currentCompany().getId());
  }

  @Override
  public WarehouseResponse create(final WarehouseRequest request) {
    return warehouseService.create(currentCompany(), request);
  }

  @Override
  @Transactional
  public WarehouseResponse update(final Long id, final WarehouseRequest request) {
    final Long companyId = currentCompany().getId();
    final Warehouse current = warehouseService.findEntityByIdAndCompanyIdForUpdate(id, companyId);
    if (current.getType() != request.getType()
        && (current.getType() == WarehouseType.TRANSIT
            || request.getType() == WarehouseType.TRANSIT)
        && stockBalanceService.hasNonZeroBalanceForWarehouse(id)) {
      throw new ApiException(WarehouseMessageKey.WAREHOUSE_TYPE_CANNOT_CHANGE_WITH_STOCK);
    }
    return warehouseService.update(id, companyId, request);
  }

  @Transactional
  @Override
  public WarehouseResponse archive(final Long id) {
    final Long companyId = currentCompany().getId();
    final Warehouse warehouse = warehouseService.findEntityByIdAndCompanyIdForUpdate(id, companyId);
    if (stockBalanceService.hasNonZeroBalanceForWarehouse(warehouse.getId())) {
      throw new ApiException(WarehouseMessageKey.WAREHOUSE_HAS_NON_ZERO_BALANCE);
    }
    return warehouseService.archive(warehouse);
  }

  @Override
  public WarehouseResponse restore(final Long id) {
    return warehouseService.restore(id, currentCompany().getId());
  }

  private Company currentCompany() {
    return currentCompanyProvider.getCurrentCompany();
  }
}
