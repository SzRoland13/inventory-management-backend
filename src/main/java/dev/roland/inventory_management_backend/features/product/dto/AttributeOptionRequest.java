package dev.roland.inventory_management_backend.features.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.product.message.ProductValidationMessage;

/** Creates a selectable value for a fixed product attribute. */
public record AttributeOptionRequest(
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 150, message = ProductValidationMessage.TOO_LONG)
        String value,
    @PositiveOrZero(message = ProductValidationMessage.NON_NEGATIVE) Integer sortOrder) {}
