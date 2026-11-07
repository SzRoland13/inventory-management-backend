package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Settlement owner assignment for a finalized invoice affected by a correction.
 *
 * @param version expected document version
 * @param ownerId settlement owner identifier
 */
public record SettlementAssignment(@NotNull Long version, @NotNull Long ownerId) {}
