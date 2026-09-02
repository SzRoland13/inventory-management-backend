package dev.roland.inventory_management_backend.features.unit.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.repository.UnitRepository;
import dev.roland.inventory_management_backend.features.unit.service.UnitService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {
  private final UnitRepository unitRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<Unit, Long> getRepository() {
    return unitRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.UNIT;
  }
}
