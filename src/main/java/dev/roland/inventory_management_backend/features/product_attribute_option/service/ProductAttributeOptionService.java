package dev.roland.inventory_management_backend.features.product_attribute_option.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;

/** Defines operations supported by the product attribute option feature. */
public interface ProductAttributeOptionService extends BaseService<ProductAttributeOption, Long> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param definitionId the attribute definition identifier
   * @return the matching resources
   */
  List<ProductAttributeOption> findAllByDefinitionId(Long definitionId);

  /**
   * Finds matching records using the supplied criteria.
   *
   * @param id the resource identifier
   * @param definitionId the attribute definition identifier
   * @return the matching resource, if present
   */
  Optional<ProductAttributeOption> findByIdAndDefinitionId(Long id, Long definitionId);

  /**
   * Checks whether a matching record exists.
   *
   * @param definitionId the attribute definition identifier
   * @return true if a matching record exists
   */
  boolean existsByDefinitionId(Long definitionId);

  /**
   * Checks whether a matching record exists.
   *
   * @param definitionId the attribute definition identifier
   * @param value the option value
   * @return true if a matching record exists
   */
  boolean existsByDefinitionIdAndValue(Long definitionId, String value);

  /**
   * Checks whether a matching record exists.
   *
   * @param definitionId the attribute definition identifier
   * @param value the option value
   * @param id the resource identifier
   * @return true if a matching record exists
   */
  boolean existsByDefinitionIdAndValueAndIdNot(Long definitionId, String value, Long id);
}
