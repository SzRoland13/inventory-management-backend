package dev.roland.inventory_management_backend.service;

import org.springframework.security.core.Authentication;

import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.PasswordSetupRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.exception.ApiException;

public interface AuthService {

  /**
   * This method checks if provided credentials are valid and if it is first login (missing
   * password)
   *
   * @param request email address of user
   * @throws ApiException if user is not registered
   * @return success or failure ApiResponse based on if the user is trying to log in first time or
   *     not
   */
  CheckFirstLoginResponse checkIfFirstLogin(EmailRequest request);

  /**
   * This method checks if provided credentials are valid and if it is then saves the new password
   * of user
   *
   * @param request email of user and the password two times
   * @throws ApiException if user credentials are invalid or the two passwords do not match
   */
  void handleSetupOfNewPassword(PasswordSetupRequest request);

  /**
   * Validates the current authenticated user session.
   *
   * @param auth the {@link Authentication} object automatically injected by Spring Security,
   *     representing the currently authenticated user
   * @throws ApiException if the authentication is missing, invalid, or the principal cannot be
   *     resolved
   */
  UserDto checkSession(Authentication auth);
}
