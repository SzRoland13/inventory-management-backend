package dev.roland.inventory_management_backend.exception;

import dev.roland.inventory_management_backend.messageKey.MessageKey;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(MessageKey messageKey) {
        super(messageKey);
    }
}
