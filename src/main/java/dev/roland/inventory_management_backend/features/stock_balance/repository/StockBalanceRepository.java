package dev.roland.inventory_management_backend.features.stock_balance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

/** Looks up inventory balances by warehouse and product. */
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
  List<StockBalance> findAllByProductId(Long productId);

  List<StockBalance> findAllByProductIdIn(List<Long> productIds);

  /**
   * Finds the stock balance for a warehouse and product.
   *
   * @param warehouse warehouse holding the stock
   * @param product product whose balance is requested
   * @return matching balance, if one exists
   */
  Optional<StockBalance> findByWarehouseAndProduct(Warehouse warehouse, Product product);
}
