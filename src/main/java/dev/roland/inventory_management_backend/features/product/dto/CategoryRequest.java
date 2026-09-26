package dev.roland.inventory_management_backend.features.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.product.message.ProductValidationMessage;

/** Creates or updates a product category. */
public record CategoryRequest(
    @Positive(message = ProductValidationMessage.POSITIVE) Long parentId,
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 100, message = ProductValidationMessage.TOO_LONG)
        String code,
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 150, message = ProductValidationMessage.TOO_LONG)
        String name,
    String description,
    @PositiveOrZero(message = ProductValidationMessage.NON_NEGATIVE) Integer sortOrder) {}
