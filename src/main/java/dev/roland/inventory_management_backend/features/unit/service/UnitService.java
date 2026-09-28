package dev.roland.inventory_management_backend.features.unit.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.unit.Unit;

/** Defines operations supported by the unit of measure feature. */
public interface UnitService extends BaseService<Unit, Long> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param companyId the company identifier
   * @return the matching resources
   */
  List<Unit> findSelectableUnits(Long companyId);

  /**
   * Finds matching records using the supplied criteria.
   *
   * @param id the resource identifier
   * @return the matching resource, if present
   */
  Optional<Unit> findById(Long id);

  /**
   * Checks whether a matching record exists.
   *
   * @param companyId the company identifier
   * @param code the resource code
   * @return true if a matching record exists
   */
  boolean existsByCompanyIdAndCode(Long companyId, String code);

  /**
   * Checks whether a matching record exists.
   *
   * @param code the resource code
   * @return true if a matching record exists
   */
  boolean existsBySystemTrueAndCode(String code);
}
