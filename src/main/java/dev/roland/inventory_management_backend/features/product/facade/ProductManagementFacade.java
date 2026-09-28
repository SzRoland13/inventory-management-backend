package dev.roland.inventory_management_backend.features.product.facade;

import dev.roland.inventory_management_backend.common.dto.PageResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductListRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductResponse;

/** Product catalog operations including company scoping and relationship validation. */
public interface ProductManagementFacade {
  /**
   * Lists the requested resources.
   *
   * @param includeArchived whether archived products are included
   * @param request the validated request
   * @return the result of the operation
   */
  PageResponse<ProductResponse> list(boolean includeArchived, ProductListRequest request);

  /**
   * Retrieves the requested resource.
   *
   * @param id the resource identifier
   * @param includeArchived whether archived products are included
   * @return the resource representation
   */
  ProductResponse get(Long id, boolean includeArchived);

  /**
   * Creates the requested resource.
   *
   * @param request the validated request
   * @return the resource representation
   */
  ProductResponse create(ProductRequest request);

  /**
   * Updates the requested resource.
   *
   * @param id the resource identifier
   * @param request the validated request
   * @return the resource representation
   */
  ProductResponse update(Long id, ProductRequest request);

  /**
   * Archives the requested resource.
   *
   * @param id the resource identifier
   */
  void archive(Long id);

  /**
   * Restores the requested resource.
   *
   * @param id the resource identifier
   * @return the resource representation
   */
  ProductResponse restore(Long id);
}
