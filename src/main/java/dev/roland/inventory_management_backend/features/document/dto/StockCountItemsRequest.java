package dev.roland.inventory_management_backend.features.document.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Request to add product lines to a draft stock count.
 *
 * @param productIds product identifiers to add
 * @param version current stock count version
 */
public record StockCountItemsRequest(
    @NotNull(message = ValidationKeys.REQUIRED) @Size(max = 100, message = ValidationKeys.TOO_LONG)
        String version,
    @NotEmpty(message = ValidationKeys.REQUIRED)
        List<
                @NotNull(message = ValidationKeys.REQUIRED)
                @Positive(message = ValidationKeys.INVALID_VALUE) Long>
            productIds) {
  /** Copies the product identifier list to protect the request value from later mutation. */
  public StockCountItemsRequest {
    productIds = productIds == null ? null : new ArrayList<>(productIds);
  }

  /**
   * Returns a copy of the requested product identifiers.
   *
   * @return a defensive copy of the product identifier list
   */
  @Override
  public List<Long> productIds() {
    return productIds == null ? null : new ArrayList<>(productIds);
  }
}
