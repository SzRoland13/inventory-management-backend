package dev.roland.inventory_management_backend.features.warehouse.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

/** Persists and queries warehouses within a company. */
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
  /**
   * Lists company warehouses by name.
   *
   * @param companyId company identifier
   * @return warehouses ordered by name
   */
  List<Warehouse> findAllByCompanyIdOrderByNameAsc(Long companyId);

  /**
   * Finds a warehouse only when it belongs to the company.
   *
   * @param id warehouse identifier
   * @param companyId company identifier
   * @return matching warehouse, if found
   */
  Optional<Warehouse> findByIdAndCompanyId(Long id, Long companyId);

  /**
   * Locks a warehouse for an update transaction.
   *
   * @param id warehouse identifier
   * @return matching locked warehouse, if found
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select warehouse from Warehouse warehouse where warehouse.id = :id")
  Optional<Warehouse> findByIdForUpdate(@Param("id") Long id);

  /**
   * Locks a company-owned warehouse for an update transaction.
   *
   * @param id warehouse identifier
   * @param companyId company identifier
   * @return matching locked warehouse, if found
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select warehouse from Warehouse warehouse "
          + "where warehouse.id = :id and warehouse.company.id = :companyId")
  Optional<Warehouse> findByIdAndCompanyIdForUpdate(
      @Param("id") Long id, @Param("companyId") Long companyId);

  /**
   * Checks whether the company already has a warehouse with this name.
   *
   * @param companyId company identifier
   * @param name warehouse name
   * @return whether the name already exists
   */
  boolean existsByCompanyIdAndName(Long companyId, String name);

  /**
   * Checks for a same-name warehouse other than the given warehouse.
   *
   * @param companyId company identifier
   * @param name warehouse name
   * @param id warehouse to exclude
   * @return whether another warehouse has the name
   */
  boolean existsByCompanyIdAndNameAndIdNot(Long companyId, String name, Long id);

  /**
   * Checks whether the company already has a warehouse with this code.
   *
   * @param companyId company identifier
   * @param code warehouse code
   * @return whether the code already exists
   */
  boolean existsByCompanyIdAndCode(Long companyId, String code);

  /**
   * Checks for a same-code warehouse other than the given warehouse.
   *
   * @param companyId company identifier
   * @param code warehouse code
   * @param id warehouse to exclude
   * @return whether another warehouse has the code
   */
  boolean existsByCompanyIdAndCodeAndIdNot(Long companyId, String code, Long id);
}
