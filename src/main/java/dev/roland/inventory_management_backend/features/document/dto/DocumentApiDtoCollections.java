package dev.roland.inventory_management_backend.features.document.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Defensive-copy helpers shared by document API DTOs. */
final class DocumentApiDtoCollections {
  private DocumentApiDtoCollections() {}

  /**
   * Copies the supplied list into an immutable list.
   *
   * @param values values to copy, or {@code null}
   * @param <T> element type
   * @return immutable copy, or {@code null}
   */
  static <T> List<T> immutableList(final List<T> values) {
    return values == null ? null : Collections.unmodifiableList(new ArrayList<>(values));
  }

  /**
   * Copies the supplied map into an immutable map.
   *
   * @param values values to copy, or {@code null}
   * @param <K> key type
   * @param <V> value type
   * @return immutable copy, or {@code null}
   */
  static <K, V> Map<K, V> immutableMap(final Map<K, V> values) {
    return values == null ? null : Collections.unmodifiableMap(new HashMap<>(values));
  }
}
