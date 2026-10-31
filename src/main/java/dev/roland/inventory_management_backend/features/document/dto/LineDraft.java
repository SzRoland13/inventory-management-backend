package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

/**
 * Defines the line draft document API contract.
 *
 * @param productId product identifier value
 * @param quantity quantity value
 * @param unitId unit identifier value
 * @param unitPrice unit price value
 */
public record LineDraft(Long productId, Long unitId, BigDecimal quantity, BigDecimal unitPrice) {}
