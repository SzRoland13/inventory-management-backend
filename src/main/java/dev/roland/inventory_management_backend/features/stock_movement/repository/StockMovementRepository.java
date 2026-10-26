package dev.roland.inventory_management_backend.features.stock_movement.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementQueryRow;

/** Provides database queries for stock movement records. */
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
  /** Finds company movements using optional product, warehouse, and timestamp filters. */
  @Query(
      value =
          "select m.id as id, m.createdAt as date, p.id as productId, p.sku as productSku, "
              + "p.name as productName, w.id as warehouseId, w.name as warehouseName, "
              + "m.quantityChange as quantityChange, m.movementType as movementType, "
              + "d.id as documentId, d.referenceNumber as documentNumber, line.id as documentLineId, "
              + "p.unit.id as mainUnitId, p.unit.code as mainUnitCode, "
              + "p.unit.name as mainUnitName, p.unit.symbol as mainUnitSymbol, "
              + "p.unit.system as mainUnitSystem, secondary.id as secondaryUnitId, "
              + "secondary.code as secondaryUnitCode, secondary.name as secondaryUnitName, "
              + "secondary.symbol as secondaryUnitSymbol, secondary.system as secondaryUnitSystem, "
              + "p.secondaryUnitsPerMainUnit as secondaryUnitsPerMainUnit "
              + "from StockMovement m join m.product p join m.warehouse w "
              + "left join p.secondaryUnit secondary "
              + "left join m.document d left join m.documentLine line "
              + "where p.company.id = :companyId and w.company.id = :companyId "
              + "and (:productId is null or p.id = :productId) "
              + "and (:warehouseId is null or w.id = :warehouseId) "
              + "and (:startDate is null or m.createdAt >= :startDate) "
              + "and (:endDate is null or m.createdAt < :endDate) "
              + "order by m.createdAt desc, m.id desc",
      countQuery =
          "select count(m.id) from StockMovement m join m.product p join m.warehouse w "
              + "where p.company.id = :companyId and w.company.id = :companyId "
              + "and (:productId is null or p.id = :productId) "
              + "and (:warehouseId is null or w.id = :warehouseId) "
              + "and (:startDate is null or m.createdAt >= :startDate) "
              + "and (:endDate is null or m.createdAt < :endDate)")
  Page<StockMovementQueryRow> findCompanyMovements(
      @Param("companyId") Long companyId,
      @Param("productId") Long productId,
      @Param("warehouseId") Long warehouseId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  /**
   * Checks whether a matching record exists.
   *
   * @param productId the product identifier
   * @return true if a matching record exists
   */
  boolean existsByProductId(Long productId);
}
