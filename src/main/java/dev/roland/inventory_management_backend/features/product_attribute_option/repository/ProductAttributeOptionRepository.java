package dev.roland.inventory_management_backend.features.product_attribute_option.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;

/** Provides database queries for product attribute option records. */
public interface ProductAttributeOptionRepository
    extends JpaRepository<ProductAttributeOption, Long> {
  List<ProductAttributeOption> findAllByDefinitionId(Long definitionId);

  boolean existsByDefinitionId(Long definitionId);

  boolean existsByDefinitionIdAndValue(Long definitionId, String value);

  boolean existsByDefinitionIdAndValueAndIdNot(Long definitionId, String value, Long id);
}
