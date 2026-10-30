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
  String MOVEMENT_SELECT =
      "select m.id as id, m.createdAt as date, p.id as productId, p.sku as productSku, "
          + "p.name as productName, w.id as warehouseId, w.name as warehouseName, "
          + "m.quantityChange as quantityChange, m.movementType as movementType, "
          + "d.id as documentId, d.referenceNumber as documentNumber, line.id as documentLineId, "
          + "p.unit.id as mainUnitId, p.unit.code as mainUnitCode, "
          + "p.unit.name as mainUnitName, p.unit.symbol as mainUnitSymbol, "
          + "p.unit.system as mainUnitSystem, secondary.id as secondaryUnitId, "
          + "secondary.code as secondaryUnitCode, secondary.name as secondaryUnitName, "
          + "secondary.symbol as secondaryUnitSymbol, secondary.system as secondaryUnitSystem, "
          + "p.secondaryUnitsPerMainUnit as secondaryUnitsPerMainUnit ";
  String MOVEMENT_JOINS =
      "from StockMovement m join m.product p join m.warehouse w "
          + "left join p.secondaryUnit secondary "
          + "left join m.document d left join m.documentLine line ";
  String MOVEMENT_FILTERS =
      "where p.company.id = :companyId and w.company.id = :companyId "
          + "and (:productId is null or p.id = :productId) "
          + "and (:warehouseId is null or w.id = :warehouseId) ";
  String MOVEMENT_ORDER = "order by m.createdAt desc, m.id desc";
  String MOVEMENT_COUNT_SELECT = "select count(m.id) ";
  String MOVEMENT_COUNT_JOINS = "from StockMovement m join m.product p join m.warehouse w ";

  /**
   * Adds date predicates only for the dates supplied by the caller.
   *
   * @param companyId owning company identifier
   * @param productId optional product filter
   * @param warehouseId optional warehouse filter
   * @param startDate inclusive lower timestamp, if supplied
   * @param endDate exclusive upper timestamp, if supplied
   * @param pageable page and sort settings
   * @return matching movement rows
   */
  default Page<StockMovementQueryRow> findCompanyMovements(
      final Long companyId,
      final Long productId,
      final Long warehouseId,
      final LocalDateTime startDate,
      final LocalDateTime endDate,
      final Pageable pageable) {
    final Page<StockMovementQueryRow> result;
    if (startDate == null && endDate == null) {
      result = findCompanyMovementsWithoutDates(companyId, productId, warehouseId, pageable);
    } else if (startDate == null) {
      result = findCompanyMovementsBeforeDate(companyId, productId, warehouseId, endDate, pageable);
    } else if (endDate == null) {
      result = findCompanyMovementsFromDate(companyId, productId, warehouseId, startDate, pageable);
    } else {
      result =
          findCompanyMovementsBetweenDates(
              companyId, productId, warehouseId, startDate, endDate, pageable);
    }
    return result;
  }

  /**
   * Queries company movements without date restrictions.
   *
   * @param companyId owning company identifier
   * @param productId optional product filter
   * @param warehouseId optional warehouse filter
   * @param pageable page and sort settings
   * @return matching movement rows
   */
  @Query(
      value = MOVEMENT_SELECT + MOVEMENT_JOINS + MOVEMENT_FILTERS + MOVEMENT_ORDER,
      countQuery = MOVEMENT_COUNT_SELECT + MOVEMENT_COUNT_JOINS + MOVEMENT_FILTERS)
  Page<StockMovementQueryRow> findCompanyMovementsWithoutDates(
      @Param("companyId") Long companyId,
      @Param("productId") Long productId,
      @Param("warehouseId") Long warehouseId,
      Pageable pageable);

  /**
   * Queries company movements from the inclusive start timestamp.
   *
   * @param companyId owning company identifier
   * @param productId optional product filter
   * @param warehouseId optional warehouse filter
   * @param startDate inclusive lower timestamp
   * @param pageable page and sort settings
   * @return matching movement rows
   */
  @Query(
      value =
          MOVEMENT_SELECT
              + MOVEMENT_JOINS
              + MOVEMENT_FILTERS
              + "and m.createdAt >= :startDate "
              + MOVEMENT_ORDER,
      countQuery =
          MOVEMENT_COUNT_SELECT
              + MOVEMENT_COUNT_JOINS
              + MOVEMENT_FILTERS
              + "and m.createdAt >= :startDate")
  Page<StockMovementQueryRow> findCompanyMovementsFromDate(
      @Param("companyId") Long companyId,
      @Param("productId") Long productId,
      @Param("warehouseId") Long warehouseId,
      @Param("startDate") LocalDateTime startDate,
      Pageable pageable);

  /**
   * Queries company movements before the exclusive end timestamp.
   *
   * @param companyId owning company identifier
   * @param productId optional product filter
   * @param warehouseId optional warehouse filter
   * @param endDate exclusive upper timestamp
   * @param pageable page and sort settings
   * @return matching movement rows
   */
  @Query(
      value =
          MOVEMENT_SELECT
              + MOVEMENT_JOINS
              + MOVEMENT_FILTERS
              + "and m.createdAt < :endDate "
              + MOVEMENT_ORDER,
      countQuery =
          MOVEMENT_COUNT_SELECT
              + MOVEMENT_COUNT_JOINS
              + MOVEMENT_FILTERS
              + "and m.createdAt < :endDate")
  Page<StockMovementQueryRow> findCompanyMovementsBeforeDate(
      @Param("companyId") Long companyId,
      @Param("productId") Long productId,
      @Param("warehouseId") Long warehouseId,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  /**
   * Queries company movements within the inclusive start and exclusive end timestamps.
   *
   * @param companyId owning company identifier
   * @param productId optional product filter
   * @param warehouseId optional warehouse filter
   * @param startDate inclusive lower timestamp
   * @param endDate exclusive upper timestamp
   * @param pageable page and sort settings
   * @return matching movement rows
   */
  @Query(
      value =
          MOVEMENT_SELECT
              + MOVEMENT_JOINS
              + MOVEMENT_FILTERS
              + "and m.createdAt >= :startDate and m.createdAt < :endDate "
              + MOVEMENT_ORDER,
      countQuery =
          MOVEMENT_COUNT_SELECT
              + MOVEMENT_COUNT_JOINS
              + MOVEMENT_FILTERS
              + "and m.createdAt >= :startDate and m.createdAt < :endDate")
  Page<StockMovementQueryRow> findCompanyMovementsBetweenDates(
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
