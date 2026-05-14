package dev.roland.inventory_management_backend.messageKey;

import lombok.Getter;

@Getter
public enum UserMessageKey implements MessageKey {
  REGISTRATION_SUCCESSFUL("user.registration-successful"),
  TWO_FA_SETUP_RESET_COMPLETE("user.two-fa-setup-reset-complete"),
  INVALID_ROLE("user.invalid-role"),
  UPDATE_SUCCESS("user.update-success"),
  USER_NOT_FOUND("user.not-found"),
  USER_ALREADY_SUSPENDED("user.already-suspended"),
  USER_NOT_SUSPENDED("user.not-suspended"),
  USER_SUSPENDED("user.suspended"),
  USER_ACTIVATED("user.activated"),
  PASSWORD_RESET_COMPLETE("user.password-reset-complete"),
  TWO_FA_NOT_ENABLED("user.two-fa-not-enabled"),
  PASSWORD_NOT_SET("user.password-not-set"),
  AVATAR_UPDATED("user.avatar-updated"),
  ;

  private final String key;

  UserMessageKey(String key) {
    this.key = key;
  }
}
