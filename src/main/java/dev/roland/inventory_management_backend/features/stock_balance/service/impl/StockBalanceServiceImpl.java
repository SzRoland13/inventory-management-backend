package dev.roland.inventory_management_backend.features.stock_balance.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceListRequest;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceQueryRow;
import dev.roland.inventory_management_backend.features.stock_balance.repository.StockBalanceRepository;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import lombok.RequiredArgsConstructor;

/** Implements the stock balance service operations. */
@Service
@RequiredArgsConstructor
public class StockBalanceServiceImpl implements StockBalanceService {
  private final StockBalanceRepository stockBalanceRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<StockBalance, Long> getRepository() {
    return stockBalanceRepository;
  }

  @Override
  public Page<StockBalanceQueryRow> findCompanyStockRows(
      final Long companyId, final StockBalanceListRequest request, final Pageable pageable) {
    final String search =
        request.search() == null || request.search().isBlank() ? null : request.search().trim();
    return stockBalanceRepository.findCompanyStockRows(
        companyId,
        ProductStatus.ACTIVE,
        WarehouseStatus.ACTIVE,
        request.productId(),
        request.warehouseId(),
        search,
        pageable);
  }

  /**
   * {@inheritDoc}
   *
   * @return get not found message key result
   */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.STOCK_BALANCE;
  }

  @Override
  public List<StockBalance> findAllByProductId(final Long id) {
    return stockBalanceRepository.findAllByProductId(id);
  }

  @Override
  public List<StockBalance> findAllByProductIdIn(final List<Long> productIds) {
    return stockBalanceRepository.findAllByProductIdIn(productIds);
  }

  @Override
  public boolean hasNonZeroBalanceForWarehouse(final Long warehouseId) {
    return stockBalanceRepository.existsNonZeroOrNullQuantityByWarehouseId(warehouseId);
  }
}
