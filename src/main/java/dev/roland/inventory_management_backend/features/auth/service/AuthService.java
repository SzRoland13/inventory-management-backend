package dev.roland.inventory_management_backend.features.auth.service;

import org.springframework.security.core.Authentication;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.auth.dto.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.features.auth.dto.EmailRequest;
import dev.roland.inventory_management_backend.features.auth.dto.PasswordSetupRequest;
import dev.roland.inventory_management_backend.features.user.dto.UserDto;

/** Defines authentication operations exposed to the API layer. */
public interface AuthService {

  /**
   * Checks whether the supplied email belongs to an account that has not set a password.
   *
   * @param request email address of user
   * @return first-login status for the requested email
   * @throws ApiException if user is not registered
   */
  CheckFirstLoginResponse checkIfFirstLogin(EmailRequest request);

  /**
   * Saves the new password after validating first-login credentials.
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
