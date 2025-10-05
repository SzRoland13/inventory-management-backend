package dev.roland.inventory_management_backend.messageKey;

public enum AuthMessageKey implements MessageKey {

    LOGIN_SUCCESS("auth.login.success"),
    INVALID_CREDENTIALS("auth.login.invalid-credentials"),
    NOT_FIRST_LOGIN("auth.login.not-first-login"),
    PASSWORDS_NOT_MATCH("auth.login.password-not-match"),
    PASSWORD_SETUP_SUCCESS("auth.login.password-setup-success"),
    EMAIL_SEND_FAILED("auth.login.email-send-failed"),
    TOKEN_REFRESHED("auth.token-refreshed"),
    INVALID_TOKEN("auth.invalid-token"),
    TOKEN_EXPIRED("auth.token-expired"),
    ONE_TIME_CODE_VALIDATION_SUCCESS("auth.one-time-code-validation-success"),
    ONE_TIME_CODE_EXPIRED("auth.one-time-code-expired");

    private final String key;

    AuthMessageKey(String key) { this.key = key; }

    @Override
    public String getKey() { return key; }
}
