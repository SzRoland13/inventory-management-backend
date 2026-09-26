package dev.roland.inventory_management_backend.features.auth.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.features.auth.CustomUserDetails;
import dev.roland.inventory_management_backend.features.auth.dto.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.features.auth.dto.EmailRequest;
import dev.roland.inventory_management_backend.features.auth.dto.PasswordSetupRequest;
import dev.roland.inventory_management_backend.features.auth.message.AuthMessageKey;
import dev.roland.inventory_management_backend.features.auth.service.AuthService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.dto.UserDto;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

/** Implements password setup and authenticated-session operations. */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  /**
   * Checks whether the supplied email belongs to an account that has not set a password.
   *
   * @param request email address of user
   * @return ApiResponse based on if the user is trying to log in first time or not
   * @throws ApiException if user is not registered
   */
  @Override
  public CheckFirstLoginResponse checkIfFirstLogin(final EmailRequest request) {
    final User user = userService.findUserByEmailOrThrow(request.getEmail());

    return new CheckFirstLoginResponse(true, user.getPassword() == null);
  }

  /**
   * Saves the new password after validating first-login credentials.
   *
   * @param request email of user and the password two times
   * @throws ApiException if user credentials are invalid or the two passwords do not match
   */
  @Override
  public void handleSetupOfNewPassword(final PasswordSetupRequest request) {
    final User user = userService.findUserByEmailOrThrow(request.getEmail());

    if (user.getPassword() != null || user.isOtcSetupComplete()) {
      throw new ApiException(AuthMessageKey.NOT_FIRST_LOGIN);
    }

    if (!request.getPassword().equals(request.getRepeatPassword())) {
      throw new ApiException(AuthMessageKey.PASSWORDS_NOT_MATCH);
    }

    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setOtcSetupComplete(true);

    userService.save(user);
  }

  /**
   * Validates the current authenticated user session.
   *
   * @param authentication the {@link Authentication} object automatically injected by Spring
   *     Security, representing the currently authenticated user
   * @return check session result
   * @throws UnauthorizedException if the authentication is missing, invalid, or the principal
   *     cannot be resolved
   */
  @Override
  public UserDto checkSession(final Authentication authentication) {

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException(AuthMessageKey.INVALID_TOKEN);
    }

    final CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    return new UserDto(userService.findByIdOrThrow(userDetails.getUserId()));
  }
}
