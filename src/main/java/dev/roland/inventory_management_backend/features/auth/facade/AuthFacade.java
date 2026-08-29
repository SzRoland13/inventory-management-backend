package dev.roland.inventory_management_backend.features.auth.facade;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.FirstLoginValidationRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginFinalizationResult;
import dev.roland.inventory_management_backend.dto.auth.LoginRequest;
import dev.roland.inventory_management_backend.dto.auth.LogoutResult;
import dev.roland.inventory_management_backend.dto.auth.ShortLifeTokenResponse;
import dev.roland.inventory_management_backend.dto.auth.TokenRefreshResult;
import dev.roland.inventory_management_backend.dto.auth.TwoFactorVerifyRequest;

/** Coordinates authentication flows that span multiple authentication services. */
public interface AuthFacade {

  /**
   * Handles first-time login by verifying credentials and sending a one-time code.
   *
   * @param request user's email address
   * @throws ApiException if user does not exist or is not a first-time login
   */
  void sendOneTimeCode(EmailRequest request);

  /**
   * Validates user's first login one time code
   *
   * @param request user's email and one time code
   * @throws ApiException if user does not exist or one time code invalid
   */
  void validateOneTimeCode(FirstLoginValidationRequest request);

  /**
   * Handles login by verifying credentials and generating auth tokens
   *
   * @param request user's email address and password
   * @return {@link ShortLifeTokenResponse} to later provide with 2FA login
   * @throws ApiException if user does not exist or provided credentials are invalid
   */
  ShortLifeTokenResponse handleLogin(LoginRequest request);

  /**
   * Handles token refresh by validating token validity and expiry
   *
   * @param token refresh token
   * @return token refresh result containing the new access token
   * @throws ApiException if token does not exist or the token expired
   */
  TokenRefreshResult handleTokenRefresh(String token);

  /**
   * Initializes Two-Factor Authentication (2FA) setup for a user by generating a TOTP secret.
   *
   * @param request contains the user's email
   * @return {@link String} with the generated QR code image (Base64 data URI)
   * @throws ApiException if user does not exist or already has 2FA enabled
   */
  String setup2fa(EmailRequest request);

  /**
   * Validates a user's TOTP code during the login flow.
   *
   * @param request contains the user's email and the TOTP code
   * @return {@link LoginFinalizationResult} containing user details
   * @throws ApiException if the user does not exist or the TOTP code is invalid
   */
  LoginFinalizationResult verify2faLogin(TwoFactorVerifyRequest request);

  /**
   * Handles user logout by clearing auth cookies.
   *
   * @param refreshToken the refresh token from cookie
   * @return logout result describing which authentication cookies to clear
   */
  LogoutResult handleLogout(String refreshToken);
}
