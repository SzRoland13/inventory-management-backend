package dev.roland.inventory_management_backend.features.product.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.dto.ProductListRequest;

/** Defines operations supported by the product catalog feature. */
public interface ProductService extends BaseService<Product, Long> {
  Page<Product> search(Long companyId, boolean archived, ProductListRequest request);

  Optional<Product> findByIdAndCompanyId(Long id, Long companyId);

  boolean existsByCompanyIdAndSku(Long companyId, String sku);

  boolean existsByCompanyIdAndSkuAndIdNot(Long companyId, String sku, Long id);

  boolean existsUsingUnit(Long unitId);
}
