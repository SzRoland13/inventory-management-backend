package dev.roland.inventory_management_backend.features.product_category_assignment.service;

import java.util.List;

import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignment;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignmentId;

/** Defines operations supported by the product category assignment feature. */
public interface ProductCategoryAssignmentService {
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
   * Processes the supplied request.
   *
   * @param assignment the product-category assignment
   * @return the result of the operation
   */
  ProductCategoryAssignment save(ProductCategoryAssignment assignment);

  /**
   * Deletes the matching record or records.
   *
   * @param productId the product identifier
   */
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
