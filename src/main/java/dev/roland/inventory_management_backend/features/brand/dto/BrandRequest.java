package dev.roland.inventory_management_backend.features.brand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.brand.message.BrandValidationMessage;

/**
 * Brand create and update payload.
 *
 * @param name brand display name
 */
public record BrandRequest(
    @NotBlank(message = BrandValidationMessage.REQUIRED)
        @Size(max = 100, message = BrandValidationMessage.TOO_LONG)
        String name) {}
