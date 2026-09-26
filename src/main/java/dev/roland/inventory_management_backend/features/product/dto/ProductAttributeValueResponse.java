package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;

/**
 * Returns one product attribute value without serializing its JPA relationships.
 *
 * @param booleanValue boolean attribute value
 * @param dateValue date attribute value
 * @param definitionCode attribute definition code
 * @param definitionId attribute definition identifier
 * @param definitionName attribute definition name
 * @param numberValue numeric attribute value
 * @param optionId selected option identifier
 * @param optionValue selected option value
 * @param textValue text attribute value
 * @param valueType attribute value type
 */
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
