package dev.roland.inventory_management_backend.features.product_category_assignment.service;

import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignment;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignmentId;

public interface ProductCategoryAssignmentService {
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
