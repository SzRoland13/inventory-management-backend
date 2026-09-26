package dev.roland.inventory_management_backend.features.product_category_assignment.service;

import java.util.List;

import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignment;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignmentId;

/** Defines operations supported by the product category assignment feature. */
public interface ProductCategoryAssignmentService {
  List<ProductCategoryAssignment> findAllByProductId(Long productId);

  List<ProductCategoryAssignment> findAllByProductIdIn(List<Long> productIds);

  ProductCategoryAssignment save(ProductCategoryAssignment assignment);

  void deleteAllByProductId(Long productId);

  /**
   * Assigns a product to a category if the assignment does not already exist.
   *
   * @param productId product id to assign
   * @param categoryId category id to assign to
   * @return existing or newly created assignment
   */
  ProductCategoryAssignment assign(Long productId, Long categoryId);

  /**
   * Removes a product-category assignment by its composite id.
   *
   * @param id product-category assignment id
   */
  void remove(ProductCategoryAssignmentId id);
}
