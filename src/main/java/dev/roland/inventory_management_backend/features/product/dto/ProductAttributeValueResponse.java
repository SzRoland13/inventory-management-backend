package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;

/** Returns one product attribute value without serializing its JPA relationships. */
public record ProductAttributeValueResponse(
    Long definitionId,
    String definitionCode,
    String definitionName,
    ProductAttributeValueType valueType,
    Long optionId,
    String optionValue,
    String textValue,
    BigDecimal numberValue,
    LocalDate dateValue,
    Boolean booleanValue) {}
