package dev.roland.inventory_management_backend.exception;

import dev.roland.inventory_management_backend.messageKey.MessageKey;

import java.util.Map;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(MessageKey messageKey) {
        super(messageKey);
    }

    public UnauthorizedException(MessageKey messageKey, Map<String, Object> params) {
        super(messageKey, params);
    }
}
