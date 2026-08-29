package dev.roland.inventory_management_backend.common.exception;

import java.util.Map;

import dev.roland.inventory_management_backend.common.message.MessageKey;

/** API exception indicating that the request is not authorized. */
public class UnauthorizedException extends ApiException {
  /** Creates an unauthorized exception. */
  public UnauthorizedException(MessageKey messageKey) {
    super(messageKey);
  }

  /** Creates an unauthorized exception. */
  public UnauthorizedException(MessageKey messageKey, Map<String, Object> params) {
    super(messageKey, params);
  }
}
