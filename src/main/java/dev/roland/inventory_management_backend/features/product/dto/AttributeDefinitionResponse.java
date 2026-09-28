package dev.roland.inventory_management_backend.features.product.dto;

import java.util.List;

import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;

/**
 * Product attribute definition and its selectable options.
 *
 * @param code unit or entity code
 * @param id unique identifier
 * @param name display name
 * @param options available attribute options
 * @param required whether the attribute is required
 * @param valueType attribute value type
 */
public record AttributeDefinitionResponse(
    Long id,
    String code,
    String name,
    ProductAttributeValueType valueType,
    boolean required,
    List<AttributeOptionResponse> options) {

  /** Copies selectable options to keep the response immutable. */
  public AttributeDefinitionResponse {
    options = List.copyOf(options);
  }
}
