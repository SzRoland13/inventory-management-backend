package dev.roland.inventory_management_backend.features.product_category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_category.ProductCategory;

/** Provides database queries for product category records. */
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
  List<ProductCategory> findAllByCompanyId(Long companyId);

  Optional<ProductCategory> findByIdAndCompanyId(Long id, Long companyId);

  boolean existsByCompanyIdAndCode(Long companyId, String code);
}
