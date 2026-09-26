package dev.roland.inventory_management_backend.common.exception;

import java.util.Map;

import dev.roland.inventory_management_backend.common.message.MessageKey;

/** API exception indicating that a requested resource does not exist. */
public class NotFoundException extends ApiException {
  /**
   * Creates a not-found exception.
   *
   * @param message localized message associated with the missing resource
   */
  public NotFoundException(MessageKey message) {
    super(message);
  }

  /**
   * Creates a not-found exception with message parameters.
   *
   * @param messageKey localized message associated with the missing resource
   * @param params values used to interpolate the message
   */
  public NotFoundException(MessageKey messageKey, Map<String, Object> params) {
    super(messageKey, params);
  }
}
