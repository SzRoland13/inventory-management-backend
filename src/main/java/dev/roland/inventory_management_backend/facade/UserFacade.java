package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.AllUserResponse;
import dev.roland.inventory_management_backend.dto.user.UserDto;

/** Coordinates user administration and profile operations. */
public interface UserFacade {

  /**
   * Handles new user registration.
   *
   * @param request user details for registration.
   * @return created user entity.
   */
  UserDto registerUser(AddEditUserRequest request);

  /**
   * Handles 2FA reset for a single user.
   *
   * @param id user id to reset the 2fa for.
   */
  void resetUser2FA(Long id);

  /**
   * Suspends a user and resets their password and 2FA.
   *
   * @param id user id to suspend.
   */
  void suspendUser(Long id);

  /**
   * Activates a suspended user.
   *
   * @param id user id to activate.
   */
  void activateUser(Long id);

  /**
   * Resets a user's password and 2FA.
   *
   * @param id user id to reset password for.
   */
  void resetPassword(Long id);

  /**
   * Associates a media asset with a user as the user's avatar.
   *
   * @param id user identifier
   * @param mediaAssetId media asset identifier
   */
  void updateAvatar(Long id, Long mediaAssetId);

  /**
   * Returns all the saved users.
   *
   * @return a {@link java.util.List} of {@link UserDto}
   */
  AllUserResponse getAllUsers();
}
