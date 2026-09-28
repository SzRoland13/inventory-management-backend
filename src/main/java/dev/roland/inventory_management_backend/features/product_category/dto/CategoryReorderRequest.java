package dev.roland.inventory_management_backend.features.product_category.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.features.product.message.ProductValidationMessage;

/**
 * Supplies the complete ordered list of category identifiers for one sibling group.
 *
 * @param categoryIds category identifiers in display order
 */
public record CategoryReorderRequest(
    @NotEmpty(message = ProductValidationMessage.REQUIRED)
        List<
                @NotNull(message = ProductValidationMessage.REQUIRED)
                @Positive(message = ProductValidationMessage.POSITIVE) Long>
            categoryIds) {
  /** Copies the supplied identifiers to prevent callers from mutating the request afterward. */
  public CategoryReorderRequest {
    categoryIds =
        categoryIds == null ? null : Collections.unmodifiableList(new ArrayList<>(categoryIds));
  }
}
