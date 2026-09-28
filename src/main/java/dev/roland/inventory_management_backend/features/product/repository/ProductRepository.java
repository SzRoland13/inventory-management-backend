package dev.roland.inventory_management_backend.features.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.roland.inventory_management_backend.features.product.Product;

/** Provides database queries for product catalog records. */
public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

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
   * Checks whether any product uses the supplied unit.
   *
   * @param unitId the unit identifier
   * @return true if a product uses the unit
   */
  @Query(
      "select case when count(p) > 0 then true else false end from Product p "
          + "where p.unit.id = :unitId or p.secondaryUnit.id = :unitId")
  boolean existsUsingUnit(@Param("unitId") Long unitId);
}
