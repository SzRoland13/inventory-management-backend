package dev.roland.inventory_management_backend.features.stock_balance.service.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceListRequest;
import dev.roland.inventory_management_backend.features.stock_balance.repository.StockBalanceRepository;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;

@ExtendWith(MockitoExtension.class)
class StockBalanceServiceImplTest {
  @Mock private StockBalanceRepository repository;
  @InjectMocks private StockBalanceServiceImpl service;

  @Test
  void normalizesMissingBlankAndPaddedSearchTerms() {
    final var pageable = PageRequest.of(0, 25);
    when(repository.findCompanyStockRows(
            1L, ProductStatus.ACTIVE, WarehouseStatus.ACTIVE, null, null, null, pageable))
        .thenReturn(new PageImpl<>(java.util.List.of()));
    when(repository.findCompanyStockRows(
            1L, ProductStatus.ACTIVE, WarehouseStatus.ACTIVE, null, null, "item", pageable))
        .thenReturn(new PageImpl<>(java.util.List.of()));

    service.findCompanyStockRows(
        1L, new StockBalanceListRequest(0, 25, null, null, null), pageable);
    service.findCompanyStockRows(
        1L, new StockBalanceListRequest(0, 25, "  ", null, null), pageable);
    service.findCompanyStockRows(
        1L, new StockBalanceListRequest(0, 25, " item ", null, null), pageable);

    verify(repository, times(2))
        .findCompanyStockRows(
            1L, ProductStatus.ACTIVE, WarehouseStatus.ACTIVE, null, null, null, pageable);
    verify(repository)
        .findCompanyStockRows(
            1L, ProductStatus.ACTIVE, WarehouseStatus.ACTIVE, null, null, "item", pageable);
  }
}
