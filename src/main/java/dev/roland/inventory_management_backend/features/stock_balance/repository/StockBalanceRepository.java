package dev.roland.inventory_management_backend.features.stock_balance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceQueryRow;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;

/** Looks up inventory balances by warehouse and product. */
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
  /** Paged balance rows, including zero rows for active product/warehouse pairs. */
  @Query(
      value =
          "select p.id as productId, p.sku as productSku, p.name as productName, "
              + "w.id as warehouseId, w.name as warehouseName, coalesce(b.quantity, 0) as quantity, "
              + "p.unit.id as mainUnitId, p.unit.code as mainUnitCode, "
              + "p.unit.name as mainUnitName, p.unit.symbol as mainUnitSymbol, "
              + "p.unit.system as mainUnitSystem, secondary.id as secondaryUnitId, "
              + "secondary.code as secondaryUnitCode, secondary.name as secondaryUnitName, "
              + "secondary.symbol as secondaryUnitSymbol, secondary.system as secondaryUnitSystem, "
              + "p.secondaryUnitsPerMainUnit as secondaryUnitsPerMainUnit "
              + "from Product p cross join Warehouse w "
              + "left join p.secondaryUnit secondary "
              + "left join StockBalance b on b.product = p and b.warehouse = w "
              + "where p.company.id = :companyId and p.deletedAt is null and p.status = :activeProduct "
              + "and w.company.id = :companyId and w.status = :activeWarehouse "
              + "and (:productId is null or p.id = :productId) "
              + "and (:warehouseId is null or w.id = :warehouseId) "
              + "and (:search is null or :search = '' or "
              + "lower(p.sku) like lower(concat('%', :search, '%')) or "
              + "lower(p.name) like lower(concat('%', :search, '%'))) "
              + "order by p.name asc, p.id asc, w.name asc, w.id asc",
      countQuery =
          "select count(p.id) from Product p cross join Warehouse w "
              + "where p.company.id = :companyId and p.deletedAt is null and p.status = :activeProduct "
              + "and w.company.id = :companyId and w.status = :activeWarehouse "
              + "and (:productId is null or p.id = :productId) "
              + "and (:warehouseId is null or w.id = :warehouseId) "
              + "and (:search is null or :search = '' or "
              + "lower(p.sku) like lower(concat('%', :search, '%')) or "
              + "lower(p.name) like lower(concat('%', :search, '%'))) ")
  Page<StockBalanceQueryRow> findCompanyStockRows(
      @Param("companyId") Long companyId,
      @Param("activeProduct") ProductStatus activeProduct,
      @Param("activeWarehouse") WarehouseStatus activeWarehouse,
      @Param("productId") Long productId,
      @Param("warehouseId") Long warehouseId,
      @Param("search") String search,
      Pageable pageable);

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
