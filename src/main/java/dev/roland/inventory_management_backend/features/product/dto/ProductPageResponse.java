package dev.roland.inventory_management_backend.features.product.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/** Product page payload with stable pagination metadata. */
public record ProductPageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last) {

  public static <T> ProductPageResponse<T> from(final Page<T> page) {
    return new ProductPageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }
}
