package dev.roland.inventory_management_backend.features.product_category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_category.ProductCategory;

/** Provides database queries for product category records. */
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param companyId the company identifier
   * @return the matching resources
   */
  List<ProductCategory> findAllByCompanyId(Long companyId);

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
