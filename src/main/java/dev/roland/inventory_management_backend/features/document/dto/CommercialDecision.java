package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Approval or rejection decision for a commercial draft.
 *
 * @param version expected document version
 * @param comment decision reason
 */
public record CommercialDecision(@NotNull Long version, @NotBlank String comment) {}
