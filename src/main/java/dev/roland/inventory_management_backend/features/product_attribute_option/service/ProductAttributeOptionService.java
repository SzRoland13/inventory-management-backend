package dev.roland.inventory_management_backend.features.product_attribute_option.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;

/** Defines operations supported by the product attribute option feature. */
public interface ProductAttributeOptionService extends BaseService<ProductAttributeOption, Long> {
  List<ProductAttributeOption> findAllByDefinitionId(Long definitionId);

  Optional<ProductAttributeOption> findByIdAndDefinitionId(Long id, Long definitionId);

  boolean existsByDefinitionId(Long definitionId);

  boolean existsByDefinitionIdAndValue(Long definitionId, String value);

  boolean existsByDefinitionIdAndValueAndIdNot(Long definitionId, String value, Long id);
}
