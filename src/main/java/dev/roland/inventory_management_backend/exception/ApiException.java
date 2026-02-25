package dev.roland.inventory_management_backend.exception;

import dev.roland.inventory_management_backend.messageKey.MessageKey;
import lombok.Getter;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException {

    private final MessageKey messageKey;
    private final Map<String, Object> params;

    public ApiException(MessageKey messageKey) {
        super(messageKey.getKey());
        this.messageKey = messageKey;
        this.params = null;
    }

    public ApiException(MessageKey messageKey, Map<String, Object> params) {
        super(messageKey.getKey());
        this.messageKey = messageKey;
        this.params = params;
    }
}
