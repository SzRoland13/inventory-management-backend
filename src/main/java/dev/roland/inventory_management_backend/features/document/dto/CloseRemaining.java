package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Defines the close remaining document API contract.
 *
 * @param reason reason value
 */
public record CloseRemaining(
    @NotBlank(message = ValidationKeys.REQUIRED) @Size(max = 500, message = ValidationKeys.TOO_LONG)
        String reason) {}
