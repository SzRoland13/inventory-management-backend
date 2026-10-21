package dev.roland.inventory_management_backend.features.warehouse.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseResponse;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.message.WarehouseMessageKey;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;

@ExtendWith(MockitoExtension.class)
class WarehouseFacadeImplTest {

  @Mock private WarehouseService warehouseService;
  @Mock private CompanyService companyService;
  @Mock private StockBalanceService stockBalanceService;

  private WarehouseFacadeImpl facade;
  private Company company;
  private Warehouse warehouse;

  @BeforeEach
  void setUp() {
    facade = new WarehouseFacadeImpl(warehouseService, companyService, stockBalanceService);
    company = Company.builder().id(7L).build();
    warehouse = Warehouse.builder().id(11L).company(company).status(WarehouseStatus.ACTIVE).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(warehouseService.findEntityByIdAndCompanyIdForUpdate(11L, company.getId()))
        .thenReturn(warehouse);
  }

  @Test
  void archivesWarehouseWhenAllBalancesAreZero() {
    when(stockBalanceService.hasNonZeroBalanceForWarehouse(11L)).thenReturn(false);
    when(warehouseService.archive(warehouse)).thenReturn(WarehouseResponse.builder().build());

    facade.archive(11L);

    verify(warehouseService).archive(warehouse);
  }

  @Test
  void rejectsArchivingWhenAnyBalanceIsNonZero() {
    when(stockBalanceService.hasNonZeroBalanceForWarehouse(11L)).thenReturn(true);

    final ApiException exception = assertThrows(ApiException.class, () -> facade.archive(11L));

    assertEquals(WarehouseMessageKey.WAREHOUSE_HAS_NON_ZERO_BALANCE, exception.getMessageKey());
    verify(warehouseService, never()).archive(warehouse);
  }
}
