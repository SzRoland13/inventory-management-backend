package dev.roland.inventory_management_backend.features.stock_balance.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceListRequest;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceQueryRow;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

/** Defines operations supported by the stock balance feature. */
public interface StockBalanceService extends BaseService<StockBalance, Long> {
  /**
   * Finds the stock balance for the specified warehouse and product.
   *
   * @param warehouse warehouse holding the stock
   * @param product product whose quantity is requested
   * @return matching balance, if found
   */
  Optional<StockBalance> findByWarehouseAndProduct(Warehouse warehouse, Product product);

  /**
   * Finds company balance rows, including computed zero rows for active pairs.
   *
   * @param companyId owning company identifier
   * @param request requested filters
   * @param pageable page and sort settings
   * @return matching balance rows
   */
  Page<StockBalanceQueryRow> findCompanyStockRows(
      Long companyId, StockBalanceListRequest request, Pageable pageable);

  /**
   * Finds matching records using the supplied criteria.
   *
   * @param productId the product identifier
   * @return the matching resources
   */
  List<StockBalance> findAllByProductId(Long productId);

  /**
   * Finds matching records using the supplied criteria.
   *
   * @param productIds the product identifiers
   * @return the matching resources
   */
  List<StockBalance> findAllByProductIdIn(List<Long> productIds);

  /**
   * Returns whether a warehouse has any non-zero or null product balance.
   *
   * @param warehouseId warehouse identifier
   * @return whether a non-zero or null balance exists
   */
  boolean hasNonZeroBalanceForWarehouse(Long warehouseId);
}
