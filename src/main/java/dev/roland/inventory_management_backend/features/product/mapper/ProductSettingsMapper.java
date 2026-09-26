package dev.roland.inventory_management_backend.features.product.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionResponse;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionResponse;
import dev.roland.inventory_management_backend.features.product.dto.CategoryResponse;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;
import dev.roland.inventory_management_backend.features.unit.Unit;

/** Maps product settings entities to their API response representations. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductSettingsMapper {

  /**
   * Maps a unit entity to its response representation.
   *
   * @param unit unit to map
   * @return mapped unit response
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "code", source = "code")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "symbol", source = "symbol")
  @Mapping(target = "system", source = "system")
  UnitResponse toUnitResponse(Unit unit);

  /**
   * Maps a category entity while exposing its parent identifier only.
   *
   * @param category category to map
   * @return mapped category response
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "parentId", source = "parent.id")
  @Mapping(target = "code", source = "code")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "sortOrder", source = "sortOrder")
  CategoryResponse toCategoryResponse(ProductCategory category);

  /**
   * Maps an attribute definition and its already-loaded options.
   *
   * @param definition definition to map
   * @param options already-mapped options
   * @return mapped attribute definition response
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "definition.id")
  @Mapping(target = "code", source = "definition.code")
  @Mapping(target = "name", source = "definition.name")
  @Mapping(target = "valueType", source = "definition.valueType")
  @Mapping(target = "required", source = "definition.required")
  @Mapping(target = "options", source = "options")
  AttributeDefinitionResponse toAttributeDefinitionResponse(
      ProductAttributeDefinition definition, List<AttributeOptionResponse> options);

  /**
   * Maps an attribute option to its response representation.
   *
   * @param option option to map
   * @return mapped option response
   */
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "id")
  @Mapping(target = "value", source = "value")
  @Mapping(target = "sortOrder", source = "sortOrder")
  AttributeOptionResponse toAttributeOptionResponse(ProductAttributeOption option);
}
