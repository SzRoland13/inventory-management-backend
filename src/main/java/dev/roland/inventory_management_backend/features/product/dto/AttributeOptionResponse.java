package dev.roland.inventory_management_backend.features.product.dto;

/** Selectable value in an attribute definition. */
public record AttributeOptionResponse(Long id, String value, Integer sortOrder) {}
