package dev.roland.inventory_management_backend.messageKey;

import lombok.Getter;

@Getter
public enum AuthMessageKey implements MessageKey {
  ACCOUNT_SUSPENDED("auth.account-suspended"),
  LOGIN_SUCCESS("auth.login.success"),
  LOGOUT_SUCCESS("auth.logut.success"),
  INVALID_CREDENTIALS("auth.login.invalid-credentials"),
  NOT_FIRST_LOGIN("auth.login.not-first-login"),
  FIRST_LOGIN("auth.login.first-login"),
  PASSWORDS_NOT_MATCH("auth.login.password-not-match"),
  PASSWORD_SETUP_SUCCESS("auth.login.password-setup-success"),
  EMAIL_SEND_FAILED("auth.login.email-send-failed"),
  TOKEN_REFRESHED("auth.token-refreshed"),
  TOKEN_VALID("auth.token-valid"),
  INVALID_TOKEN("auth.invalid-token"),
  TOKEN_EXPIRED("auth.token-expired"),
  TWO_FA_ALREADY_ENABLED("auth.two-fa-already-enabled"),
  TWO_FA_CODE_GENERATED("auth.two-fa-code-generated"),
  INVALID_TWO_FA_CODE("auth.invalid-two-fa-code"),
  INVALID_OR_EXPIRED_SESSION("auth.invalid-or-expired-session"),
  TWO_FA_SETUP_COMPLETE("auth.two-fa-setup-complete"),
  TWO_FA_NOT_ENABLED("auth.two-fa-not-enabled"),
  PASSWORD_VALID_NEEDS_TWO_FA("auth.password-valid-needs-two-fa"),
  ONE_TIME_CODE_SENT("auth.one-time-code-sent"),
  ONE_TIME_CODE_VALIDATION_SUCCESS("auth.one-time-code-validation-success"),
  ONE_TIME_CODE_EXPIRED("auth.one-time-code-expired"),
  ONE_TIME_CODE_SHOULD_BE_VERIFIED_FIRST("auth.one-time-code.validate-before-two-fa"),
  ;

  private final String key;

  AuthMessageKey(String key) {
    this.key = key;
  }
}
