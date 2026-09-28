package dev.roland.inventory_management_backend.features.product_category_assignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignment;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignmentId;

/** Provides database queries for product category assignment records. */
public interface ProductCategoryAssignmentRepository
    extends JpaRepository<ProductCategoryAssignment, ProductCategoryAssignmentId> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param productId the product identifier
   * @return the matching resources
   */
  List<ProductCategoryAssignment> findAllByProductId(Long productId);

  /**
   * Finds matching records using the supplied criteria.
   *
   * @param productIds the product identifiers
   * @return the matching resources
   */
  List<ProductCategoryAssignment> findAllByProductIdIn(List<Long> productIds);

  /**
   * Deletes the matching record or records.
   *
   * @param productId the product identifier
   */
  void deleteAllByProductId(Long productId);
}
