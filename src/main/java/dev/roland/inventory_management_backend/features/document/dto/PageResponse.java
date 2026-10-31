package dev.roland.inventory_management_backend.features.document.dto;

import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableList;
import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableMap;

import java.util.List;
import java.util.Map;

/**
 * Defines the page response document API contract.
 *
 * @param counts counts value
 * @param items items value
 * @param page page value
 * @param pageSize page size value
 * @param total total value
 */
public record PageResponse(
    List<DocumentResponse> items, long total, int page, int pageSize, Map<String, Long> counts) {
  /**
   * Copies response collections to prevent external mutation.
   *
   * @param items page items
   * @param total total item count
   * @param page page index
   * @param pageSize page size
   * @param counts counts by status
   */
  public PageResponse {
    items = immutableList(items);
    counts = immutableMap(counts);
  }

  /**
   * Returns a defensive copy of the page items.
   *
   * @return immutable page items
   */
  @Override
  public List<DocumentResponse> items() {
    return immutableList(items);
  }

  /**
   * Returns a defensive copy of the page counts.
   *
   * @return immutable page counts
   */
  @Override
  public Map<String, Long> counts() {
    return immutableMap(counts);
  }
}
