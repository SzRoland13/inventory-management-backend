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
  List<UnitResponse> listUnits();

  UnitResponse createUnit(UnitRequest request);

  UnitResponse updateUnit(Long id, UnitRequest request);

  void deleteUnit(Long id);

  List<CategoryResponse> listCategories();

  CategoryResponse createCategory(CategoryRequest request);

  CategoryResponse updateCategory(Long id, CategoryRequest request);

  void deleteCategory(Long id);

  List<AttributeDefinitionResponse> listDefinitions();

  AttributeDefinitionResponse createDefinition(AttributeDefinitionRequest request);

  AttributeDefinitionResponse updateDefinition(Long id, AttributeDefinitionRequest request);

  void deleteDefinition(Long id);

  AttributeOptionResponse createOption(Long definitionId, AttributeOptionRequest request);

  AttributeOptionResponse updateOption(
      Long definitionId, Long optionId, AttributeOptionRequest request);

  void deleteOption(Long definitionId, Long optionId);
}
