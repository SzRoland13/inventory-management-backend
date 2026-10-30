package dev.roland.inventory_management_backend.features.product.dto;

/** Product image reference and its position in the product gallery. */
public record ProductImageResponse(Long mediaAssetId, Integer sortOrder, String filename) {}
