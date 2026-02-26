package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;

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
}
