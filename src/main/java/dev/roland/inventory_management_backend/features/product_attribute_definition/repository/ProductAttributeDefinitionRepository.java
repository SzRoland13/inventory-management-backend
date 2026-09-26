package dev.roland.inventory_management_backend.features.product_attribute_definition.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;

/** Provides database queries for product attribute definition records. */
public interface ProductAttributeDefinitionRepository
    extends JpaRepository<ProductAttributeDefinition, Long> {
  List<ProductAttributeDefinition> findAllByCompanyId(Long companyId);

  Optional<ProductAttributeDefinition> findByIdAndCompanyId(Long id, Long companyId);

  boolean existsByCompanyIdAndCode(Long companyId, String code);
}
