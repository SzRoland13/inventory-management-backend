package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Approval or rejection decision for a commercial draft.
 *
 * @param version expected document version
 * @param comment decision reason
 */
public record CommercialDecision(
    @NotNull(message = ValidationKeys.REQUIRED) @Positive(message = ValidationKeys.INVALID_VALUE)
        Long version,
    @NotBlank(message = ValidationKeys.REQUIRED)
        @Size(max = 2000, message = ValidationKeys.TOO_LONG)
        String comment) {}
