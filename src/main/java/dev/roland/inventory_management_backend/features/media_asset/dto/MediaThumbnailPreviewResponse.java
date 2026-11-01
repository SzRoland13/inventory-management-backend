package dev.roland.inventory_management_backend.features.media_asset.dto;

import java.time.Instant;

import dev.roland.inventory_management_backend.features.media_asset.MediaThumbnailStatus;

/**
 * Thumbnail preview state and temporary URL when the derivative is ready.
 *
 * @param id media asset identifier
 * @param status thumbnail generation status
 * @param getUrl temporary URL when the thumbnail is ready
 * @param expiry URL expiration timestamp
 */
public record MediaThumbnailPreviewResponse(
    Long id, MediaThumbnailStatus status, String getUrl, Instant expiry) {}
