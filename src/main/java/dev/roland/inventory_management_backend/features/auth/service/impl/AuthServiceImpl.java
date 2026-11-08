package dev.roland.inventory_management_backend.features.auth.service.impl;

import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.features.auth.CustomUserDetails;
import dev.roland.inventory_management_backend.features.auth.dto.PasswordSetupRequest;
import dev.roland.inventory_management_backend.features.auth.message.AuthMessageKey;
import dev.roland.inventory_management_backend.features.auth.service.AuthService;
import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCode;
import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCodeTokens;
import dev.roland.inventory_management_backend.features.one_time_code.service.OneTimeCodeService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.dto.UserDto;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

/** Implements invitation password setup and authenticated-session operations. */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final OneTimeCodeService oneTimeCodeService;

  /**
   * Saves the new password after validating the emailed one-time token.
   *
   * @param request one-time token and the password two times
   * @throws ApiException if user credentials are invalid or the two passwords do not match
   */
  @Override
  @Transactional
  public void handleSetupOfNewPassword(final PasswordSetupRequest request) {
    if (!request.getPassword().equals(request.getRepeatPassword())) {
      throw new ApiException(AuthMessageKey.PASSWORDS_NOT_MATCH);
    }

    final OneTimeCode oneTimeCode =
        oneTimeCodeService
            .findByCodeHashForUpdate(OneTimeCodeTokens.hash(request.getOneTimeCode()))
            .orElse(null);

    if (oneTimeCode == null || !oneTimeCode.getExpiresAt().isAfter(LocalDateTime.now())) {
      throw new ApiException(AuthMessageKey.INVALID_OR_EXPIRED_SESSION);
    }

    final User user = oneTimeCode.getUser();

    if (user.getPassword() != null || user.isOtcSetupComplete()) {
      throw new ApiException(AuthMessageKey.NOT_FIRST_LOGIN);
    }

    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setOtcSetupComplete(true);

    userService.save(user);
    oneTimeCodeService.delete(oneTimeCode);
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
