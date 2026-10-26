package dev.roland.inventory_management_backend.features.stock_balance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

/** Looks up inventory balances by warehouse and product. */
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
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
   * Finds the stock balance for a warehouse and product.
   *
   * @param warehouse warehouse holding the stock
   * @param product product whose balance is requested
   * @return matching balance, if one exists
   */
  Optional<StockBalance> findByWarehouseAndProduct(Warehouse warehouse, Product product);

  /**
   * Checks whether a warehouse has any non-zero or null stock quantity.
   *
   * @param warehouseId warehouse identifier
   * @return whether a non-zero or null balance exists
   */
  @Query(
      "select count(balance) > 0 from StockBalance balance "
          + "where balance.warehouse.id = :warehouseId "
          + "and (balance.quantity is null or balance.quantity <> 0)")
  boolean existsNonZeroOrNullQuantityByWarehouseId(@Param("warehouseId") Long warehouseId);
}
