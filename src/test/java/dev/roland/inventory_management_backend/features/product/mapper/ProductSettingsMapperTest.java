package dev.roland.inventory_management_backend.features.product.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionResponse;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionResponse;
import dev.roland.inventory_management_backend.features.product.dto.CategoryResponse;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;
import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;

@SpringJUnitConfig(classes = ProductSettingsMapperImpl.class)
class ProductSettingsMapperTest {

  @Autowired private ProductSettingsMapper productSettingsMapper;

  @Test
  void mapsCategoryParentAsAnIdentifier() {
    final ProductCategory category =
        ProductCategory.builder()
            .id(12L)
            .parent(ProductCategory.builder().id(5L).build())
            .code("GLOVES")
            .name("Gloves")
            .description("Protective gloves")
            .sortOrder(2)
            .build();

    final CategoryResponse response = productSettingsMapper.toCategoryResponse(category);

    assertEquals(
        new CategoryResponse(12L, 5L, "GLOVES", "Gloves", "Protective gloves", 2), response);
  }

  @Test
  void mapsAttributeDefinitionAndItsOptions() {
    final ProductAttributeDefinition definition =
        ProductAttributeDefinition.builder()
            .id(3L)
            .code("COLOR")
            .name("Color")
            .valueType(ProductAttributeValueType.FIXED)
            .required(true)
            .build();
    final List<ProductAttributeOption> options =
        List.of(
            ProductAttributeOption.builder().id(7L).value("Blue").sortOrder(0).build(),
            ProductAttributeOption.builder().id(8L).value("Red").sortOrder(1).build());

    final AttributeDefinitionResponse response =
        productSettingsMapper.toAttributeDefinitionResponse(definition, options);

    assertEquals(3L, response.id());
    assertEquals("COLOR", response.code());
    assertEquals("Color", response.name());
    assertEquals(ProductAttributeValueType.FIXED, response.valueType());
    assertEquals(true, response.required());
    assertEquals(
        List.of(
            new AttributeOptionResponse(7L, "Blue", 0), new AttributeOptionResponse(8L, "Red", 1)),
        response.options());
  }
}
