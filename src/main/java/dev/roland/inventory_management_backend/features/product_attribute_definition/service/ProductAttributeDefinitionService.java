package dev.roland.inventory_management_backend.features.product_attribute_definition.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;

/** Defines operations supported by the product attribute definition feature. */
public interface ProductAttributeDefinitionService
    extends BaseService<ProductAttributeDefinition, Long> {
  List<ProductAttributeDefinition> findAllByCompanyId(Long companyId);

  Optional<ProductAttributeDefinition> findByIdAndCompanyId(Long id, Long companyId);

  boolean existsByCompanyIdAndCode(Long companyId, String code);
}
