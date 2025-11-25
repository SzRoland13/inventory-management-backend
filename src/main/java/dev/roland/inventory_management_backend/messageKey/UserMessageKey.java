package dev.roland.inventory_management_backend.messageKey;

public enum UserMessageKey implements MessageKey {

    REGISTRATION_SUCCESSFUL("user.registration-successful"),
    TWO_FA_SETUP_RESET_COMPLETE("user.two-fa-setup-reset-complete"),
    INVALID_ROLE("user.invalid-role"),
    UPDATE_SUCCESS("user.update-success"),
    USER_NOT_FOUND("user.not-found"),
    ;

    private final String key;

    UserMessageKey(String key) { this.key = key; }

    @Override
    public String getKey() { return key; }
}
