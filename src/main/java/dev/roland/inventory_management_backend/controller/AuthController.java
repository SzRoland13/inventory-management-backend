package dev.roland.inventory_management_backend.controller;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.*;
import dev.roland.inventory_management_backend.facade.AuthFacade;
import dev.roland.inventory_management_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/V1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthFacade authFacade;

    @PostMapping("/check-first-login")
    ResponseEntity<ApiResponse<CheckFirstLoginResponse>> checkIfFirstLogin(@Valid @RequestBody EmailRequest request) {
        return authService.checkIfFirstLogin(request);
    }

    @PostMapping("/send-one-time-code")
    ResponseEntity<ApiResponse<Void>> sendOneTimeCode(@Valid @RequestBody EmailRequest request) {
        return authFacade.sendOneTimeCode(request);
    }

    @PostMapping("/validate-one-time-code")
    ResponseEntity<ApiResponse<Void>> validateOneTimeCodeLogin(@Valid @RequestBody FirstLoginValidationRequest request) {
        return authFacade.validateOneTimeCodeLogin(request);
    }

    @PostMapping("/setup-password")
    ResponseEntity<ApiResponse<Void>> handleSetupOfNewPassword(@Valid @RequestBody PasswordSetupRequest request) {
        return authService.handleSetupOfNewPassword(request);
    }

    @PostMapping("/login")
    ResponseEntity<ApiResponse<ShortLivedTokenResponse>> handleLogin(@Valid @RequestBody LoginRequest request) {
        return authFacade.handleLogin(request);
    }

    @PostMapping("/refresh")
    ResponseEntity<ApiResponse<LoginResponse.TokensDetails>> handleTokenRefresh(@Valid @RequestBody RefreshRequest request) {
        return authFacade.handleTokenRefresh(request);
    }

    @PostMapping("/2fa/setup")
    ResponseEntity<ApiResponse<String>> setup2fa(@Valid @RequestBody EmailRequest request) {
        return authFacade.setup2fa(request);
    }

    @PostMapping("/2fa/verify")
    ResponseEntity<ApiResponse<Void>> verify2fa(@Valid @RequestBody TwoFactorVerifyRequest request) {
        return authFacade.verify2fa(request);
    }

    @PostMapping("/2fa/verify-login")
    ResponseEntity<ApiResponse<LoginResponse>> verify2faLogin(@Valid @RequestBody TwoFactorVerifyRequest request) {
        return authFacade.verify2faLogin(request);
    }
}