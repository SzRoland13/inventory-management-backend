package dev.roland.inventory_management_backend.features.product.dto;

import java.util.List;

import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;

/** Product attribute definition and its selectable options. */
public record AttributeDefinitionResponse(
    Long id,
    String code,
    String name,
    ProductAttributeValueType valueType,
    boolean required,
    List<AttributeOptionResponse> options) {}
