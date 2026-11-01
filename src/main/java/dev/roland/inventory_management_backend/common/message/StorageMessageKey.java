package dev.roland.inventory_management_backend.common.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Message keys for object storage failures. */
@Getter
@RequiredArgsConstructor
public enum StorageMessageKey implements MessageKey {
  OBJECT_READ_FAILED("storage.object-read-failed");

  private final String key;
}
