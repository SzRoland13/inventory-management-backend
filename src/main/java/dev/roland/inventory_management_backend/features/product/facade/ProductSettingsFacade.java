package dev.roland.inventory_management_backend.features.product.facade;

import java.util.List;

import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionResponse;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionResponse;
import dev.roland.inventory_management_backend.features.product.dto.CategoryRequest;
import dev.roland.inventory_management_backend.features.product.dto.CategoryResponse;
import dev.roland.inventory_management_backend.features.product.dto.UnitRequest;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;

/** Product catalog settings operations. */
public interface ProductSettingsFacade {
  /**
   * Lists the requested resources.
   *
   * @return the matching resources
   */
  List<UnitResponse> listUnits();

  /**
   * Creates the requested resource.
   *
   * @param request the validated request
   * @return the resource representation
   */
  UnitResponse createUnit(UnitRequest request);

  /**
   * Updates the requested resource.
   *
   * @param id the resource identifier
   * @param request the validated request
   * @return the resource representation
   */
  UnitResponse updateUnit(Long id, UnitRequest request);

  /**
   * Deletes the matching record or records.
   *
   * @param id the resource identifier
   */
  void deleteUnit(Long id);

  /**
   * Lists the requested resources.
   *
   * @return the matching resources
   */
  List<CategoryResponse> listCategories();

  /**
   * Creates the requested resource.
   *
   * @param request the validated request
   * @return the resource representation
   */
  CategoryResponse createCategory(CategoryRequest request);

  /**
   * Updates the requested resource.
   *
   * @param id the resource identifier
   * @param request the validated request
   * @return the resource representation
   */
  CategoryResponse updateCategory(Long id, CategoryRequest request);

  /**
   * Deletes the matching record or records.
   *
   * @param id the resource identifier
   */
  void deleteCategory(Long id);

  /**
   * Lists the requested resources.
   *
   * @return the matching resources
   */
  List<AttributeDefinitionResponse> listDefinitions();

  /**
   * Creates the requested resource.
   *
   * @param request the validated request
   * @return the resource representation
   */
  AttributeDefinitionResponse createDefinition(AttributeDefinitionRequest request);

  /**
   * Updates the requested resource.
   *
   * @param id the resource identifier
   * @param request the validated request
   * @return the resource representation
   */
  AttributeDefinitionResponse updateDefinition(Long id, AttributeDefinitionRequest request);

  /**
   * Deletes the matching record or records.
   *
   * @param id the resource identifier
   */
  void deleteDefinition(Long id);

  /**
   * Creates the requested resource.
   *
   * @param definitionId the attribute definition identifier
   * @param request the validated request
   * @return the resource representation
   */
  AttributeOptionResponse createOption(Long definitionId, AttributeOptionRequest request);

  /**
   * Updates the requested resource.
   *
   * @param definitionId the attribute definition identifier
   * @param optionId the attribute option identifier
   * @param request the validated request
   * @return the resource representation
   */
  AttributeOptionResponse updateOption(
      Long definitionId, Long optionId, AttributeOptionRequest request);

  /**
   * Deletes the matching record or records.
   *
   * @param definitionId the attribute definition identifier
   * @param optionId the attribute option identifier
   */
  void deleteOption(Long definitionId, Long optionId);
}
