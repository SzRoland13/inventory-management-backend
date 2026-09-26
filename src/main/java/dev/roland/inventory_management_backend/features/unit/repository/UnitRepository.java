package dev.roland.inventory_management_backend.features.unit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.unit.Unit;

/** Provides database queries for unit of measure records. */
public interface UnitRepository extends JpaRepository<Unit, Long> {}
