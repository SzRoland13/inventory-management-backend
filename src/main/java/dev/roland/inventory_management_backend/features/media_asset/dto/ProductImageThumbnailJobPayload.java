package dev.roland.inventory_management_backend.features.media_asset.dto;

/**
 * Payload identifying the media asset to thumbnail.
 *
 * @param mediaAssetId media asset identifier
 */
public record ProductImageThumbnailJobPayload(Long mediaAssetId) {}
