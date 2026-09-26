package dev.roland.inventory_management_backend.features.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.product.enumeration.ProductSortDirection;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductSortField;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.product.message.ProductValidationMessage;

/** Paging, sorting, searching, and filter controls for product catalog listing. */
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

  public ProductListRequest {
    page = page == null ? 0 : page;
    size = size == null ? 25 : size;
    sortBy = sortBy == null ? ProductSortField.NAME : sortBy;
    sortDirection = sortDirection == null ? ProductSortDirection.ASC : sortDirection;
  }
}
