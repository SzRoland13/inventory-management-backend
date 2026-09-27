package dev.roland.inventory_management_backend.features.product_category.facade;

import java.util.List;

import dev.roland.inventory_management_backend.features.product.dto.CategoryRequest;
import dev.roland.inventory_management_backend.features.product.dto.CategoryResponse;

/** Company-scoped product category operations. */
public interface ProductCategoryFacade {
  /**
   * Lists categories belonging to the current company.
   *
   * @return company categories
   */
  List<CategoryResponse> list();

  /**
   * Creates a category for the current company.
   *
   * @param request category data
   * @return created category
   */
  CategoryResponse create(CategoryRequest request);

  /**
   * Updates a category owned by the current company.
   *
   * @param id category identifier
   * @param request updated category data
   * @return updated category
   */
  CategoryResponse update(Long id, CategoryRequest request);

  /**
   * Deletes a category owned by the current company.
   *
   * @param id category identifier
   */
  void delete(Long id);
}
