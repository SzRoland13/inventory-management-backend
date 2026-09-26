package dev.roland.inventory_management_backend.features.unit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.roland.inventory_management_backend.features.unit.Unit;

/** Provides database queries for unit of measure records. */
public interface UnitRepository extends JpaRepository<Unit, Long> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param companyId the company identifier
   * @return the matching resources
   */
  @Query("select u from Unit u where u.system = true or u.company.id = :companyId order by u.code")
  List<Unit> findSelectableUnits(@Param("companyId") Long companyId);

  /**
   * Checks whether a matching record exists.
   *
   * @param code the resource code
   * @return true if a matching record exists
   */
  boolean existsBySystemTrueAndCode(String code);

  /**
   * Checks whether a matching record exists.
   *
   * @param companyId the company identifier
   * @param code the resource code
   * @return true if a matching record exists
   */
  boolean existsByCompanyIdAndCode(Long companyId, String code);
}
