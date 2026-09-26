package dev.roland.inventory_management_backend.common.exception;

import java.util.Map;

import dev.roland.inventory_management_backend.common.message.MessageKey;

/** API exception indicating that the request is not authorized. */
public class UnauthorizedException extends ApiException {
  /**
   * Creates an unauthorized exception.
   *
   * @param messageKey localized message associated with the failure
   */
  public UnauthorizedException(final MessageKey messageKey) {
    super(messageKey);
  }

  /**
   * Creates an unauthorized exception with message parameters.
   *
   * @param messageKey localized message associated with the failure
   * @param params values used to interpolate the message
   */
  public UnauthorizedException(final MessageKey messageKey, final Map<String, Object> params) {
    super(messageKey, params);
  }
}
