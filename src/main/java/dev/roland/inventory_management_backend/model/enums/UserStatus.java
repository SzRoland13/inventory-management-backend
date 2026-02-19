package dev.roland.inventory_management_backend.model.enums;

import java.util.Arrays;
import java.util.Optional;

public enum UserStatus {
    ACTIVE,
    SUSPENDED,
    SETUP_REQUIRED,
    ;

    public static Optional<UserStatus> fromString(String value) {
        return Arrays.stream(values())
                .filter(r -> r.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
