package dev.roland.inventory_management_backend.features.product.facade;

import dev.roland.inventory_management_backend.common.dto.PageResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductListRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductResponse;

/** Product catalog operations including company scoping and relationship validation. */
public interface ProductManagementFacade {
  PageResponse<ProductResponse> list(boolean includeArchived, ProductListRequest request);

  ProductResponse get(Long id, boolean includeArchived);

  ProductResponse create(ProductRequest request);

  ProductResponse update(Long id, ProductRequest request);

  void archive(Long id);

  ProductResponse restore(Long id);
}
