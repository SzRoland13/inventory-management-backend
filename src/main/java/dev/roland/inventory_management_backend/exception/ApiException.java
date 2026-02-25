package dev.roland.inventory_management_backend.exception;

import dev.roland.inventory_management_backend.messageKey.MessageKey;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final MessageKey messageKey;

    public ApiException(MessageKey messageKey) {
        super(messageKey.getKey());
        this.messageKey = messageKey;
    }
}
