package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Defines the draft update document API contract.
 *
 * @param draft draft value
 * @param version version value
 */
public record DraftUpdate(
    @NotBlank(message = ValidationKeys.REQUIRED) @Size(max = 100, message = ValidationKeys.TOO_LONG)
        String version,
    @NotNull(message = ValidationKeys.REQUIRED) @Valid Draft draft) {}
