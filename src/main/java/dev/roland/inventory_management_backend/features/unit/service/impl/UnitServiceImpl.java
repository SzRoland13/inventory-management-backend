package dev.roland.inventory_management_backend.features.unit.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.repository.UnitRepository;
import dev.roland.inventory_management_backend.features.unit.service.UnitService;
import lombok.RequiredArgsConstructor;

/** Implements the unit of measure service operations. */
@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {
  private final UnitRepository unitRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<Unit, Long> getRepository() {
    return unitRepository;
  }

  /**
   * {@inheritDoc}
   *
   * @return get not found message key result
   */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.UNIT;
  }

  @Override
  public java.util.List<Unit> findSelectableUnits(Long companyId) {
    return unitRepository.findSelectableUnits(companyId);
  }

  @Override
  public java.util.Optional<Unit> findById(Long id) {
    return unitRepository.findById(id);
  }

  @Override
  public boolean existsByCompanyIdAndCode(Long companyId, String code) {
    return unitRepository.existsByCompanyIdAndCode(companyId, code);
  }

  @Override
  public boolean existsBySystemTrueAndCode(String code) {
    return unitRepository.existsBySystemTrueAndCode(code);
  }
}
