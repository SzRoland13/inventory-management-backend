package dev.roland.inventory_management_backend.features.product_attribute_definition.facade;

import java.util.List;

import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionResponse;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionResponse;

/** Company-scoped product attribute definition and option operations. */
public interface ProductAttributeFacade {
  /**
   * Lists attribute definitions owned by the current company.
   *
   * @return company attribute definitions
   */
  List<AttributeDefinitionResponse> list();

  /**
   * Creates an attribute definition for the current company.
   *
   * @param request attribute definition data
   * @return created definition
   */
  AttributeDefinitionResponse create(AttributeDefinitionRequest request);

  /**
   * Updates an attribute definition owned by the current company.
   *
   * @param id definition identifier
   * @param request updated definition data
   * @return updated definition
   */
  AttributeDefinitionResponse update(Long id, AttributeDefinitionRequest request);

  /**
   * Deletes an attribute definition owned by the current company.
   *
   * @param id definition identifier
   */
  void delete(Long id);

  /**
   * Creates an option for a fixed attribute definition.
   *
   * @param definitionId definition identifier
   * @param request option data
   * @return created option
   */
  AttributeOptionResponse createOption(Long definitionId, AttributeOptionRequest request);

  /**
   * Updates an option belonging to the specified definition.
   *
   * @param definitionId definition identifier
   * @param optionId option identifier
   * @param request updated option data
   * @return updated option
   */
  AttributeOptionResponse updateOption(
      Long definitionId, Long optionId, AttributeOptionRequest request);

  /**
   * Deletes an unused option belonging to the specified definition.
   *
   * @param definitionId definition identifier
   * @param optionId option identifier
   */
  void deleteOption(Long definitionId, Long optionId);
}
