package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

/**
 * Defines the fulfillment line document API contract.
 *
 * @param productId product identifier value
 * @param quantity quantity value
 * @param sourceLineId source line identifier value
 */
public record FulfillmentLine(Long sourceLineId, Long productId, BigDecimal quantity) {}
