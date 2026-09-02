package dev.roland.inventory_management_backend.features.user.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;

/** Message keys for user administration and profile operations. */
@Getter
public enum UserMessageKey implements MessageKey {
  /** Message key for registration successful. */
  REGISTRATION_SUCCESSFUL("user.registration-successful"),
  /** Message key for two-factor authentication setup reset complete. */
  TWO_FA_SETUP_RESET_COMPLETE("user.two-fa-setup-reset-complete"),
  /** Message key for invalid role. */
  INVALID_ROLE("user.invalid-role"),
  /** Message key for update success. */
  UPDATE_SUCCESS("user.update-success"),
  /** Message key for user not found. */
  USER_NOT_FOUND("user.not-found"),
  /** Message key for user already suspended. */
  USER_ALREADY_SUSPENDED("user.already-suspended"),
  /** Message key for user not suspended. */
  USER_NOT_SUSPENDED("user.not-suspended"),
  /** Message key for user suspended. */
  USER_SUSPENDED("user.suspended"),
  /** Message key for user activated. */
  USER_ACTIVATED("user.activated"),
  /** Message key for password reset complete. */
  PASSWORD_RESET_COMPLETE("user.password-reset-complete"),
  /** Message key for two-factor authentication not enabled. */
  TWO_FA_NOT_ENABLED("user.two-fa-not-enabled"),
  /** Message key for password not set. */
  PASSWORD_NOT_SET("user.password-not-set"),
  /** Message key for avatar updated. */
  AVATAR_UPDATED("user.avatar-updated"),
  ;

  private final String key;

  UserMessageKey(String key) {
    this.key = key;
  }
}
