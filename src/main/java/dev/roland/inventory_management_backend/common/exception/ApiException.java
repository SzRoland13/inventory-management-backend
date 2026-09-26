package dev.roland.inventory_management_backend.common.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;

/** Base exception for API failures represented by a localized message key. */
@Getter
public class ApiException extends RuntimeException {

  private final MessageKey messageKey;
  private final Map<String, Object> params;

  /**
   * Creates an API exception without response parameters.
   *
   * @param messageKey localized message associated with the failure
   */
  public ApiException(MessageKey messageKey) {
    super(messageKey.getKey());
    this.messageKey = messageKey;
    this.params = null;
  }

  /**
   * Creates an API exception with parameters for message interpolation.
   *
   * @param messageKey localized message associated with the failure
   * @param params values used to interpolate the message
   */
  public ApiException(MessageKey messageKey, Map<String, Object> params) {
    super(messageKey.getKey());
    this.messageKey = messageKey;
    this.params = params == null ? null : Collections.unmodifiableMap(new HashMap<>(params));
  }
}
