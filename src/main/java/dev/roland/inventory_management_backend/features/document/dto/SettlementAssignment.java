package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Settlement owner assignment for a finalized invoice affected by a correction.
 *
 * @param version expected document version
 * @param ownerId settlement owner identifier
 */
public record SettlementAssignment(
    @NotNull(message = ValidationKeys.REQUIRED) @Positive(message = ValidationKeys.INVALID_VALUE)
        Long version,
    @NotNull(message = ValidationKeys.REQUIRED) @Positive(message = ValidationKeys.INVALID_VALUE)
        Long ownerId) {}
