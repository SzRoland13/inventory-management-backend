package dev.roland.inventory_management_backend.features.product.dto;

/**
 * Product category API representation.
 *
 * @param code unit or entity code
 * @param description descriptive text
 * @param id unique identifier
 * @param name display name
 * @param parentId parent category identifier
 * @param sortOrder display order
 */
public record CategoryResponse(
    Long id, Long parentId, String code, String name, String description, Integer sortOrder) {}
