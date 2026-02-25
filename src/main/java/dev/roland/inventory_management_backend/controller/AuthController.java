package dev.roland.inventory_management_backend.controller;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.FirstLoginValidationRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginResponse;
import dev.roland.inventory_management_backend.dto.auth.PasswordSetupRequest;
import dev.roland.inventory_management_backend.dto.auth.ShortLifeTokenResponse;
import dev.roland.inventory_management_backend.dto.auth.TwoFactorVerifyRequest;
import dev.roland.inventory_management_backend.facade.AuthFacade;
import dev.roland.inventory_management_backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/v1/auth")
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
    ResponseEntity<ApiResponse<Void>> validateOneTimeCode(@Valid @RequestBody FirstLoginValidationRequest request) {
        return authFacade.validateOneTimeCode(request);
    }

    @PostMapping("/setup-password")
    ResponseEntity<ApiResponse<Void>> handleSetupOfNewPassword(@Valid @RequestBody PasswordSetupRequest request) {
        return authService.handleSetupOfNewPassword(request);
    }

    @PostMapping("/login")
    ResponseEntity<ApiResponse<ShortLifeTokenResponse>> handleLogin(@Valid @RequestBody LoginRequest request) {
        return authFacade.handleLogin(request);
    }

    @PostMapping("/refresh")
    ResponseEntity<ApiResponse<Void>> handleTokenRefresh(@CookieValue(value = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {
        return authFacade.handleTokenRefresh(refreshToken, response);
    }

    @PostMapping("/2fa/setup")
    ResponseEntity<ApiResponse<String>> setup2fa(@Valid @RequestBody EmailRequest request) {
        return authFacade.setup2fa(request);
    }

    @PostMapping("/2fa/login")
    ResponseEntity<ApiResponse<LoginResponse>> verify2faLogin(@Valid @RequestBody TwoFactorVerifyRequest request, HttpServletResponse response) {
        return authFacade.verify2faLogin(request, response);
    }
}