package dev.roland.inventory_management_backend.messageKey;

public enum GenericMessageKey  implements MessageKey {
    GENERIC_ERROR("error.generic"),
    VALIDATION_ERROR("error.validation");

    private final String key;

    GenericMessageKey(String key) { this.key = key; }

    @Override
    public String getKey() { return key; }
}
