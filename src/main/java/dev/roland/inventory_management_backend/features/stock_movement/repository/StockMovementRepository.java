package dev.roland.inventory_management_backend.features.stock_movement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;

/** Provides database queries for stock movement records. */
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
  /**
   * Checks whether a matching record exists.
   *
   * @param productId the product identifier
   * @return true if a matching record exists
   */
  boolean existsByProductId(Long productId);
}
