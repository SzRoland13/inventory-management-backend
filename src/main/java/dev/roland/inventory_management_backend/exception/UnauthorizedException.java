package dev.roland.inventory_management_backend.exception;

import java.util.Map;

import dev.roland.inventory_management_backend.message_key.MessageKey;

public class UnauthorizedException extends ApiException {
  public UnauthorizedException(MessageKey messageKey) {
    super(messageKey);
  }

  public UnauthorizedException(MessageKey messageKey, Map<String, Object> params) {
    super(messageKey, params);
  }
}
