package dev.roland.inventory_management_backend.features.unit.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.unit.Unit;

/** Defines operations supported by the unit of measure feature. */
public interface UnitService extends BaseService<Unit, Long> {
  List<Unit> findSelectableUnits(Long companyId);

  Optional<Unit> findById(Long id);

  boolean existsByCompanyIdAndCode(Long companyId, String code);

  boolean existsBySystemTrueAndCode(String code);
}
