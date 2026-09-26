package dev.roland.inventory_management_backend.features.product_category_assignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignment;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignmentId;

/** Provides database queries for product category assignment records. */
public interface ProductCategoryAssignmentRepository
    extends JpaRepository<ProductCategoryAssignment, ProductCategoryAssignmentId> {
  List<ProductCategoryAssignment> findAllByProductId(Long productId);

  List<ProductCategoryAssignment> findAllByProductIdIn(List<Long> productIds);

  void deleteAllByProductId(Long productId);
}
