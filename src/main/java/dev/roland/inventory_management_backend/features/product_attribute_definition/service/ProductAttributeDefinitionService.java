package dev.roland.inventory_management_backend.features.product_attribute_definition.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;

/** Defines operations supported by the product attribute definition feature. */
public interface ProductAttributeDefinitionService
    extends BaseService<ProductAttributeDefinition, Long> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param companyId the company identifier
   * @return the matching resources
   */
  List<ProductAttributeDefinition> findAllByCompanyId(Long companyId);

  /**
   * Finds matching records using the supplied criteria.
   *
   * @param id the resource identifier
   * @param companyId the company identifier
   * @return the matching resource, if present
   */
  Optional<ProductAttributeDefinition> findByIdAndCompanyId(Long id, Long companyId);

  /**
   * Checks whether a matching record exists.
   *
   * @param companyId the company identifier
   * @param code the resource code
   * @return true if a matching record exists
   */
  boolean existsByCompanyIdAndCode(Long companyId, String code);
}
