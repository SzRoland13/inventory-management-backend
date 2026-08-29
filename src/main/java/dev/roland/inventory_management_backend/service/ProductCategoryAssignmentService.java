package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.ProductCategoryAssignment;
import dev.roland.inventory_management_backend.model.ProductCategoryAssignmentId;

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
