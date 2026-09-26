package dev.roland.inventory_management_backend.features.stock_movement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;

/** Provides database queries for stock movement records. */
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
  boolean existsByProductId(Long productId);
}
