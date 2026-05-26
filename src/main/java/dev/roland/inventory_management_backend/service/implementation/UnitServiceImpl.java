package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.Unit;
import dev.roland.inventory_management_backend.repository.UnitRepository;
import dev.roland.inventory_management_backend.service.UnitService;
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
