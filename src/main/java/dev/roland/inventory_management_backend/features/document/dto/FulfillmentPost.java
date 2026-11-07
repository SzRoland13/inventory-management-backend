package dev.roland.inventory_management_backend.features.document.dto;

import java.time.LocalDateTime;

/**
 * Optional stock-movement time supplied when posting a fulfillment document.
 *
 * @param effectiveAt optional effective movement time
 */
public record FulfillmentPost(LocalDateTime effectiveAt) {}
