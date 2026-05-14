package dev.roland.inventory_management_backend.message_key;

import lombok.Getter;

@Getter
public enum GenericMessageKey implements MessageKey {
  GENERIC_ERROR("error.generic"),
  VALIDATION_ERROR("error.validation"),
  REQUEST_SUCCESS("request.success");

  private final String key;

  GenericMessageKey(String key) {
    this.key = key;
  }
}
