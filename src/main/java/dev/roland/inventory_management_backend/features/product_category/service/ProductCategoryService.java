package dev.roland.inventory_management_backend.features.product_category.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;

/** Defines operations supported by the product category feature. */
public interface ProductCategoryService extends BaseService<ProductCategory, Long> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param companyId the company identifier
   * @return the matching resources
   */
  List<ProductCategory> findAllByCompanyId(Long companyId);

  /**
   * Persists multiple categories.
   *
   * @param categories categories to persist
   * @return saved categories
   */
  List<ProductCategory> saveAll(List<ProductCategory> categories);

  /**
   * Finds matching records using the supplied criteria.
   *
   * @param id the resource identifier
   * @param companyId the company identifier
   * @return the matching resource, if present
   */
  Optional<ProductCategory> findByIdAndCompanyId(Long id, Long companyId);

  /**
   * Checks whether a matching record exists.
   *
   * @param companyId the company identifier
   * @param code the resource code
   * @return true if a matching record exists
   */
  boolean existsByCompanyIdAndCode(Long companyId, String code);
}
