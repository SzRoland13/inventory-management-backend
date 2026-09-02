package dev.roland.inventory_management_backend.features.stock_movement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {}
