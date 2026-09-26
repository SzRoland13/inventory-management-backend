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

  Optional<Product> findByIdAndCompanyId(Long id, Long companyId);

  boolean existsByCompanyIdAndSku(Long companyId, String sku);

  boolean existsByCompanyIdAndSkuAndIdNot(Long companyId, String sku, Long id);

  @Query(
      "select case when count(p) > 0 then true else false end from Product p "
          + "where p.unit.id = :unitId or p.secondaryUnit.id = :unitId")
  boolean existsUsingUnit(@Param("unitId") Long unitId);
}
