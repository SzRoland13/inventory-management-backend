package dev.roland.inventory_management_backend.features.product.dto;

/**
 * Product image reference and its position in the product gallery.
 *
 * @param mediaAssetId referenced media asset identifier
 * @param sortOrder gallery order; the lowest value is the primary image
 * @param filename stored media filename
 */
public record ProductImageResponse(Long mediaAssetId, Integer sortOrder, String filename) {}
