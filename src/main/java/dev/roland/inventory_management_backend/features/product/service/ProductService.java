package dev.roland.inventory_management_backend.features.product.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.dto.ProductListRequest;

/** Defines operations supported by the product catalog feature. */
public interface ProductService extends BaseService<Product, Long> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param companyId the company identifier
   * @param archived whether archived products are included
   * @param request the validated request
   * @return the requested page of resources
   */
  Page<Product> search(Long companyId, boolean archived, ProductListRequest request);

  /**
   * Finds matching records using the supplied criteria.
   *
   * @param id the resource identifier
   * @param companyId the company identifier
   * @return the matching resource, if present
   */
  Optional<Product> findByIdAndCompanyId(Long id, Long companyId);

  /**
   * Checks whether a matching record exists.
   *
   * @param companyId the company identifier
   * @param sku the product SKU
   * @return true if a matching record exists
   */
  boolean existsByCompanyIdAndSku(Long companyId, String sku);

  /**
   * Checks whether a matching record exists.
   *
   * @param companyId the company identifier
   * @param sku the product SKU
   * @param id the resource identifier
   * @return true if a matching record exists
   */
  boolean existsByCompanyIdAndSkuAndIdNot(Long companyId, String sku, Long id);

  /**
   * Checks whether a matching record exists.
   *
   * @param unitId the unit identifier
   * @return true if a matching record exists
   */
  boolean existsUsingUnit(Long unitId);
}
