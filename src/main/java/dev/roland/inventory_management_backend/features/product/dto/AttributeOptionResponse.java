package dev.roland.inventory_management_backend.features.product.dto;

/**
 * Selectable value in an attribute definition.
 *
 * @param id unique identifier
 * @param sortOrder display order
 * @param value attribute option value
 */
public record AttributeOptionResponse(Long id, String value, Integer sortOrder) {}
