package dev.roland.inventory_management_backend.exception;

import java.util.Map;

import dev.roland.inventory_management_backend.messageKey.MessageKey;

public class NotFoundException extends ApiException {
  public NotFoundException(MessageKey message) {
    super(message);
  }

  public NotFoundException(MessageKey messageKey, Map<String, Object> params) {
    super(messageKey, params);
  }
}
