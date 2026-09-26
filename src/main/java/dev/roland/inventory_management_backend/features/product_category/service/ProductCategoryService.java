package dev.roland.inventory_management_backend.features.product_category.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;

/** Defines operations supported by the product category feature. */
public interface ProductCategoryService extends BaseService<ProductCategory, Long> {
  List<ProductCategory> findAllByCompanyId(Long companyId);

  Optional<ProductCategory> findByIdAndCompanyId(Long id, Long companyId);

  boolean existsByCompanyIdAndCode(Long companyId, String code);
}
