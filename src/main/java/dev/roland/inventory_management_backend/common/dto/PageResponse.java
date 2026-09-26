package dev.roland.inventory_management_backend.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/** Stable paginated response with content and page metadata. */
public record PageResponse<T>(List<T> content, PageMetadata page) {

  public static <T> PageResponse<T> from(final Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        new PageMetadata(
            page.getSize(),
            page.getNumber(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()));
  }

  /** Pagination details for the returned content. */
  public record PageMetadata(
      int size,
      int number,
      long totalElements,
      int totalPages,
      boolean first,
      boolean last) {}
}
