package dev.roland.inventory_management_backend.exception;

import java.util.Map;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import lombok.Getter;

/** Base exception for API failures represented by a localized message key. */
@Getter
public class ApiException extends RuntimeException {

  private final MessageKey messageKey;
  private final Map<String, Object> params;

  /** Creates an API exception without response parameters. */
  public ApiException(MessageKey messageKey) {
    super(messageKey.getKey());
    this.messageKey = messageKey;
    this.params = null;
  }

  /** Creates an API exception with parameters for message interpolation. */
  public ApiException(MessageKey messageKey, Map<String, Object> params) {
    super(messageKey.getKey());
    this.messageKey = messageKey;
    this.params = params;
  }
}
