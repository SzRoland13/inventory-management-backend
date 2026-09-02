package dev.roland.inventory_management_backend.features.unit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.unit.Unit;

public interface UnitRepository extends JpaRepository<Unit, Long> {}
