package dev.roland.inventory_management_backend.model.enums;

import java.util.Arrays;
import java.util.Optional;

public enum UserRole {
    ADMIN,
    MANAGER,
    SALES;

    public static Optional<UserRole> fromString(String value) {
        return Arrays.stream(values())
                .filter(r -> r.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
