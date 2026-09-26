package dev.roland.inventory_management_backend.features.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.product.enumeration.ProductSortDirection;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductSortField;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.product.message.ProductValidationMessage;

/**
 * Paging, sorting, searching, and filter controls for product catalog listing.
 *
 * @param brand brand value
 * @param categoryId category id value
 * @param page pagination metadata
 * @param search search value
 * @param size requested page size
 * @param sortBy sort by value
 * @param sortDirection sort direction value
 * @param status product status
 * @param unitId unit id value
 */
public record ProductListRequest(
    @Min(value = 0, message = ProductValidationMessage.NON_NEGATIVE) Integer page,
    @Min(value = 1, message = ProductValidationMessage.POSITIVE)
        @Max(value = 100, message = ProductValidationMessage.PAGE_SIZE_LIMIT)
        Integer size,
    @Size(max = 200, message = ProductValidationMessage.TOO_LONG) String search,
    @Size(max = 100, message = ProductValidationMessage.TOO_LONG) String brand,
    ProductStatus status,
    @Positive(message = ProductValidationMessage.POSITIVE) Long categoryId,
    @Positive(message = ProductValidationMessage.POSITIVE) Long unitId,
    ProductSortField sortBy,
    ProductSortDirection sortDirection) {

  /** Applies default values to unspecified product-list filters. */
  public ProductListRequest {
    page = page == null ? Integer.valueOf(0) : page;
    size = size == null ? Integer.valueOf(25) : size;
    sortBy = sortBy == null ? ProductSortField.NAME : sortBy;
    sortDirection = sortDirection == null ? ProductSortDirection.ASC : sortDirection;
  }
}
