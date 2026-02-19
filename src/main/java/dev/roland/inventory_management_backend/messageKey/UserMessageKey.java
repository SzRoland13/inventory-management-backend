package dev.roland.inventory_management_backend.messageKey;

public enum UserMessageKey implements MessageKey {

    REGISTRATION_SUCCESSFUL("user.registration-successful"),
    TWO_FA_SETUP_RESET_COMPLETE("user.two-fa-setup-reset-complete"),
    INVALID_ROLE("user.invalid-role"),
    UPDATE_SUCCESS("user.update-success"),
    USER_NOT_FOUND("user.not-found"),
    USER_ALREADY_SUSPENDED("user.already.suspended"),
    USER_NOT_SUSPENDED("user.not.suspended"),
    USER_SUSPENDED("user.suspended"),
    USER_ACTIVATED("user.activated"),
    PASSWORD_RESET_COMPLETE("password.reset.complete"),
    TWO_FA_NOT_ENABLED("two.fa.not.enabled"),
    ;

    private final String key;

    UserMessageKey(String key) { this.key = key; }

    @Override
    public String getKey() { return key; }
}
