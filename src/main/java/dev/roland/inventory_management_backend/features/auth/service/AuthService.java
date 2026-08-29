package dev.roland.inventory_management_backend.features.auth.service;

import org.springframework.security.core.Authentication;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.PasswordSetupRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;

public interface AuthService {

  /**
   * This method checks if provided credentials are valid and if it is first login (missing
   * password)
   *
   * @param request email address of user
   * @return first-login status for the requested email
   * @throws ApiException if user is not registered
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
   * @return current authenticated user data
   * @throws ApiException if the authentication is missing, invalid, or the principal cannot be
   *     resolved
   */
  UserDto checkSession(Authentication auth);
}
