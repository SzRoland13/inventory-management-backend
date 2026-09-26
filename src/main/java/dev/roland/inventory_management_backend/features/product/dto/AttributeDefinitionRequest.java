package dev.roland.inventory_management_backend.features.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.product.message.ProductValidationMessage;
import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;

/**
 * Creates or updates a company product attribute definition.
 *
 * @param code unit or entity code
 * @param name display name
 * @param required whether the attribute is required
 * @param valueType attribute value type
 */
public record AttributeDefinitionRequest(
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 100, message = ProductValidationMessage.TOO_LONG)
        String code,
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 150, message = ProductValidationMessage.TOO_LONG)
        String name,
    @NotNull(message = ProductValidationMessage.REQUIRED) ProductAttributeValueType valueType,
    boolean required) {}
