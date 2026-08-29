package dev.roland.inventory_management_backend.message_key;

import lombok.Getter;

/** Message keys for generic success and error responses. */
@Getter
public enum GenericMessageKey implements MessageKey {
  /** Message key for generic error. */
  GENERIC_ERROR("error.generic"),
  /** Message key for validation error. */
  VALIDATION_ERROR("error.validation"),
  /** Message key for request success. */
  REQUEST_SUCCESS("request.success");

  private final String key;

  GenericMessageKey(String key) {
    this.key = key;
  }
}
