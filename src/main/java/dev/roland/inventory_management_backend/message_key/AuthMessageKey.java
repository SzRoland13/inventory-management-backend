package dev.roland.inventory_management_backend.message_key;

import lombok.Getter;

/** Message keys for authentication, session, and two-factor-authentication responses. */
@Getter
public enum AuthMessageKey implements MessageKey {
  /** Message key for user's account is suspended. */
  ACCOUNT_SUSPENDED("auth.account-suspended"),
  /** Message key for login success. */
  LOGIN_SUCCESS("auth.login.success"),
  /** Message key for logout success. */
  LOGOUT_SUCCESS("auth.logout.success"),
  /** Message key for invalid credentials. */
  INVALID_CREDENTIALS("auth.login.invalid-credentials"),
  /** Message key for not first login. */
  NOT_FIRST_LOGIN("auth.login.not-first-login"),
  /** Message key for first login. */
  FIRST_LOGIN("auth.login.first-login"),
  /** Message key for passwords not match. */
  PASSWORDS_NOT_MATCH("auth.login.password-not-match"),
  /** Message key for password setup success. */
  PASSWORD_SETUP_SUCCESS("auth.login.password-setup-success"),
  /** Message key for email send failed. */
  EMAIL_SEND_FAILED("auth.login.email-send-failed"),
  /** Message key for token refreshed. */
  TOKEN_REFRESHED("auth.token-refreshed"),
  /** Message key for token valid. */
  TOKEN_VALID("auth.token-valid"),
  /** Message key for invalid token. */
  INVALID_TOKEN("auth.invalid-token"),
  /** Message key for token expired. */
  TOKEN_EXPIRED("auth.token-expired"),
  /** Message key for two-factor authentication already enabled. */
  TWO_FA_ALREADY_ENABLED("auth.two-fa-already-enabled"),
  /** Message key for two-factor authentication code generated. */
  TWO_FA_CODE_GENERATED("auth.two-fa-code-generated"),
  /** Message key for invalid two-factor authentication code. */
  INVALID_TWO_FA_CODE("auth.invalid-two-fa-code"),
  /** Message key for invalid or expired session. */
  INVALID_OR_EXPIRED_SESSION("auth.invalid-or-expired-session"),
  /** Message key for two-factor authentication setup complete. */
  TWO_FA_SETUP_COMPLETE("auth.two-fa-setup-complete"),
  /** Message key for two-factor authentication not enabled. */
  TWO_FA_NOT_ENABLED("auth.two-fa-not-enabled"),
  /** Message key for password valid needs two-factor authentication. */
  PASSWORD_VALID_NEEDS_TWO_FA("auth.password-valid-needs-two-fa"),
  /** Message key for one-time code sent. */
  ONE_TIME_CODE_SENT("auth.one-time-code-sent"),
  /** Message key for one-time code validation success. */
  ONE_TIME_CODE_VALIDATION_SUCCESS("auth.one-time-code-validation-success"),
  /** Message key for one-time code expired. */
  ONE_TIME_CODE_EXPIRED("auth.one-time-code-expired"),
  /** Message key for one-time code should be verified first. */
  ONE_TIME_CODE_SHOULD_BE_VERIFIED_FIRST("auth.one-time-code.validate-before-two-fa"),
  ;

  private final String key;

  AuthMessageKey(String key) {
    this.key = key;
  }
}
