package dev.roland.inventory_management_backend.exception;

import dev.roland.inventory_management_backend.messageKey.MessageKey;

public class NotFoundException extends ApiException {
    public NotFoundException(MessageKey message) {
        super(message);
    }
}
