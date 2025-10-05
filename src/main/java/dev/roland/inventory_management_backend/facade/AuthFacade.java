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
     * @return user data and generated tokens
     * @throws ApiException if user does not exist or provided credentials are invalid
     */
    ResponseEntity<ApiResponse<LoginResponse>> handleLogin(LoginRequest request);

    /**
     * Handles token refresh by validating token validity and expiry
     *
     * @param request refresh token
     * @return new access and the provided refresh token
     * @throws ApiException if token does not exist or the token expired
     */
    ResponseEntity<ApiResponse<LoginResponse.TokensDetails>> handleTokenRefresh(RefreshRequest request);

}
