package dev.roland.inventory_management_backend.common.exception;

import java.util.Map;

import dev.roland.inventory_management_backend.common.message.MessageKey;

/** API exception indicating that a requested resource does not exist. */
public class NotFoundException extends ApiException {
  /** Creates a not-found exception. */
  public NotFoundException(MessageKey message) {
    super(message);
  }

  /** Creates a not-found exception. */
  public NotFoundException(MessageKey messageKey, Map<String, Object> params) {
    super(messageKey, params);
  }
}
