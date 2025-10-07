package dev.roland.inventory_management_backend.messageKey;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final MessageKey messageKey;

    public ApiException(MessageKey messageKey) {
        super(messageKey.getKey());
        this.messageKey = messageKey;
    }
}
