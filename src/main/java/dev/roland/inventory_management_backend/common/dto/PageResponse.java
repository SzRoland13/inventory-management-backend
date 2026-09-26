package dev.roland.inventory_management_backend.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Stable paginated response with content and page metadata.
 *
 * @param <T> type of content elements
 * @param content current page content
 * @param page pagination metadata
 */
public record PageResponse<T>(List<T> content, PageMetadata page) {

  /** Copies page content to keep the response immutable. */
  public PageResponse {
    content = List.copyOf(content);
  }

  /** Creates a stable response from a Spring Data page. */
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

  /**
   * Pagination details for the returned content.
   *
   * @param size requested page size
   * @param number zero-based page number
   * @param totalElements total number of elements
   * @param totalPages total number of pages
   * @param first whether this is the first page
   * @param last whether this is the last page
   */
  public record PageMetadata(
      int size, int number, long totalElements, int totalPages, boolean first, boolean last) {}
}
