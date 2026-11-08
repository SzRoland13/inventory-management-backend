package dev.roland.inventory_management_backend.features.auth.service;

import org.springframework.security.core.Authentication;

import dev.roland.inventory_management_backend.features.auth.dto.PasswordSetupRequest;
import dev.roland.inventory_management_backend.features.user.dto.UserDto;

/** Defines authentication operations exposed to the API layer. */
public interface AuthService {

  /**
   * Saves the new password after validating the emailed one-time token.
   *
   * @param request one-time token and the password two times
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
