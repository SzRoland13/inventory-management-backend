package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.*;
import dev.roland.inventory_management_backend.messageKey.ApiException;
import org.springframework.http.ResponseEntity;

public interface AuthFacade {

    /**
     *  Handles first-time login by verifying credentials and sending a one-time code.
     *
     * @param request user's email address
     * @return ApiResponse indicating whether the email was sent successfully
     * @throws ApiException if user does not exist or is not a first-time login
     */
    ResponseEntity<ApiResponse<Void>> sendOneTimeCode(EmailRequest request);

    /**
     * Validates user's first login one time code
     * @param request user's email and one time code
     * @return ApiResponse indicating whether the one time code was valid
     * @throws ApiException if user does not exist or one time code invalid
     */
    ResponseEntity<ApiResponse<Void>> validateOneTimeCodeLogin(FirstLoginValidationRequest request);

    /**
     * Handles login by verifying credentials and generating auth tokens
     *
     * @param request user's email address and password
     * @return Short life token to later provide with 2FA login
     * @throws ApiException if user does not exist or provided credentials are invalid
     */
    ResponseEntity<ApiResponse<ShortLivedTokenResponse>> handleLogin(LoginRequest request);

    /**
     * Handles token refresh by validating token validity and expiry
     *
     * @param request refresh token
     * @return new access and the provided refresh token
     * @throws ApiException if token does not exist or the token expired
     */
    ResponseEntity<ApiResponse<LoginResponse.TokensDetails>> handleTokenRefresh(RefreshRequest request);

    /**
     * Initializes Two-Factor Authentication (2FA) setup for a user by generating a TOTP secret.
     *
     * @param request contains the user's email
     * @return ApiResponse with the generated QR code image (Base64 data URI)
     * @throws ApiException if user does not exist or already has 2FA enabled
     */
    ResponseEntity<ApiResponse<String>> setup2fa(EmailRequest request);

    /**
     * Completes the 2FA setup process by validating the user's initial TOTP code.
     *
     * @param request contains the user's email and the TOTP code for verification
     * @return ApiResponse indicating successful 2FA setup
     * @throws ApiException if the user does not exist or the TOTP code is invalid
     */
    ResponseEntity<ApiResponse<Void>> verify2fa(TwoFactorVerifyRequest request);

    /**
     * Validates a user's TOTP code during the login flow.
     *
     * @param request contains the user's email and the TOTP code
     * @return ApiResponse containing user details and generated access/refresh tokens
     * @throws ApiException if the user does not exist or the TOTP code is invalid
     */
    ResponseEntity<ApiResponse<LoginResponse>> verify2faLogin(TwoFactorVerifyRequest request);
}
