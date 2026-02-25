package dev.roland.inventory_management_backend.messageKey;

import lombok.Getter;

@Getter
public enum NotFoundMessageKey implements MessageKey {
    USER("not-found.user"),
    ONE_TIME_CODE("not-found.one-time-code"),
    REFRESH_TOKEN("not-found.refresh-token"),
    ;

    private final String key;

    NotFoundMessageKey(String key) { this.key = key; }
}
