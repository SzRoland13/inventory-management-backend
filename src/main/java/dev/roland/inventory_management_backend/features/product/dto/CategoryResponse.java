package dev.roland.inventory_management_backend.features.product.dto;

/** Product category API representation. */
public record CategoryResponse(
    Long id, Long parentId, String code, String name, String description, Integer sortOrder) {}
