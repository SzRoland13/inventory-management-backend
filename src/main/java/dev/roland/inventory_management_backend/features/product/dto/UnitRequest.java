package dev.roland.inventory_management_backend.features.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.product.message.ProductValidationMessage;

/** Creates or updates a company-owned unit. */
public record UnitRequest(
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 50, message = ProductValidationMessage.TOO_LONG)
        String code,
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 100, message = ProductValidationMessage.TOO_LONG)
        String name,
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 20, message = ProductValidationMessage.TOO_LONG)
        String symbol) {}
