package dev.roland.inventory_management_backend.controller;

import static dev.roland.inventory_management_backend.controller.AuthController.AUTH_BASE_ENDPOINT;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.FirstLoginValidationRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginFinalizationResult;
import dev.roland.inventory_management_backend.dto.auth.LoginRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginResponse;
import dev.roland.inventory_management_backend.dto.auth.LogoutResult;
import dev.roland.inventory_management_backend.dto.auth.PasswordSetupRequest;
import dev.roland.inventory_management_backend.dto.auth.ShortLifeTokenResponse;
import dev.roland.inventory_management_backend.dto.auth.TokenRefreshResult;
import dev.roland.inventory_management_backend.dto.auth.TwoFactorVerifyRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.facade.AuthFacade;
import dev.roland.inventory_management_backend.messageKey.AuthMessageKey;
import dev.roland.inventory_management_backend.messageKey.MessageKey;
import dev.roland.inventory_management_backend.service.AuthService;
import dev.roland.inventory_management_backend.service.common.HttpOnlyCookieService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(AUTH_BASE_ENDPOINT)
@RequiredArgsConstructor
public class AuthController {
  public static final String AUTH_BASE_ENDPOINT = "api/v1/auth";
  public static final String CHECK_FIRST_LOGIN_ENDPOINT = "/check-first-login";
  public static final String SEND_OTC_ENDPOINT = "/send-one-time-code";
  public static final String VALIDATE_OTC_ENDPOINT = "/validate-one-time-code";
  public static final String SETUP_PASSWORD_ENDPOINT = "/setup-password";
  public static final String LOGIN_ENDPOINT = "/login";
  public static final String REFRESH_ENDPOINT = "/refresh";
  public static final String TWO_FA_SETUP_ENDPOINT = "/2fa/setup";
  public static final String TWO_FA_LOGIN_ENDPOINT = "/2fa/login";
  public static final String CHECK_SESSION_ENDPOINT = "/check-session";
  public static final String LOGOUT_ENDPOINT = "/logout";

  private final AuthService authService;
  private final AuthFacade authFacade;
  private final HttpOnlyCookieService cookieService;
  private final AppConfiguration appConfiguration;

  @PostMapping(CHECK_FIRST_LOGIN_ENDPOINT)
  ResponseEntity<ApiResponse<CheckFirstLoginResponse>> checkIfFirstLogin(
      @Valid @RequestBody EmailRequest request) {
    CheckFirstLoginResponse response = authService.checkIfFirstLogin(request);

    AuthMessageKey messageKey =
        response.isFirstLogin() ? AuthMessageKey.FIRST_LOGIN : AuthMessageKey.NOT_FIRST_LOGIN;

    return ResponseEntity.ok(ApiResponse.success(messageKey, response));
  }

  @PostMapping(SEND_OTC_ENDPOINT)
  ResponseEntity<ApiResponse<Void>> sendOneTimeCode(@Valid @RequestBody EmailRequest request) {
    authFacade.sendOneTimeCode(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(AuthMessageKey.ONE_TIME_CODE_SENT, null));
  }

  @PostMapping(VALIDATE_OTC_ENDPOINT)
  ResponseEntity<ApiResponse<Void>> validateOneTimeCode(
      @Valid @RequestBody FirstLoginValidationRequest request) {
    authFacade.validateOneTimeCode(request);

    return ResponseEntity.ok(
        ApiResponse.success(AuthMessageKey.ONE_TIME_CODE_VALIDATION_SUCCESS, null));
  }

  @PostMapping(SETUP_PASSWORD_ENDPOINT)
  ResponseEntity<ApiResponse<Void>> handleSetupOfNewPassword(
      @Valid @RequestBody PasswordSetupRequest request) {
    authService.handleSetupOfNewPassword(request);

    return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.PASSWORD_SETUP_SUCCESS, null));
  }

  @PostMapping(LOGIN_ENDPOINT)
  ResponseEntity<ApiResponse<ShortLifeTokenResponse>> handleLogin(
      @Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok()
        .body(ApiResponse.success(AuthMessageKey.LOGIN_SUCCESS, authFacade.handleLogin(request)));
  }

  @PostMapping(REFRESH_ENDPOINT)
  ResponseEntity<ApiResponse<Void>> handleTokenRefresh(
      @CookieValue(value = "refresh_token", required = false) String refreshToken,
      HttpServletResponse response) {

    TokenRefreshResult result = authFacade.handleTokenRefresh(refreshToken);

    if (result.isShouldClearRefreshToken()) {
      cookieService.clearRefreshCookie(response, appConfiguration.isSecureCookie());
    } else if (result.getAccessToken() != null) {
      cookieService.setAccessCookie(
          response,
          result.getAccessToken(),
          appConfiguration.isSecureCookie(),
          (int) appConfiguration.getAccessTokenExpirationTime());
    }

    return result.isShouldClearRefreshToken()
        ? ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.failure(AuthMessageKey.TOKEN_EXPIRED))
        : ResponseEntity.ok(ApiResponse.success(AuthMessageKey.TOKEN_REFRESHED, null));
  }

  @PostMapping(TWO_FA_SETUP_ENDPOINT)
  ResponseEntity<ApiResponse<String>> setup2fa(@Valid @RequestBody EmailRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(AuthMessageKey.TWO_FA_CODE_GENERATED, authFacade.setup2fa(request)));
  }

  @PostMapping(TWO_FA_LOGIN_ENDPOINT)
  ResponseEntity<ApiResponse<LoginResponse>> verify2faLogin(
      @Valid @RequestBody TwoFactorVerifyRequest request, HttpServletResponse response) {

    LoginFinalizationResult result = authFacade.verify2faLogin(request);

    cookieService.setAccessCookie(
        response,
        result.getTokens().getAccessToken(),
        appConfiguration.isSecureCookie(),
        (int) appConfiguration.getAccessTokenExpirationTime());
    cookieService.setRefreshCookie(
        response,
        result.getTokens().getRefreshToken(),
        appConfiguration.isSecureCookie(),
        (int) appConfiguration.getRefreshTokenExpirationTime());

    MessageKey key =
        result.isFirstTime2faEnabled()
            ? AuthMessageKey.TWO_FA_SETUP_COMPLETE
            : AuthMessageKey.LOGIN_SUCCESS;

    return ResponseEntity.ok(ApiResponse.success(key, result.getLoginResponse()));
  }

  @GetMapping(CHECK_SESSION_ENDPOINT)
  public ResponseEntity<ApiResponse<UserDto>> checkSession(Authentication authentication) {
    return ResponseEntity.ok(
        ApiResponse.success(AuthMessageKey.TOKEN_VALID, authService.checkSession(authentication)));
  }

  @PostMapping(LOGOUT_ENDPOINT)
  ResponseEntity<ApiResponse<Void>> handleLogout(
      @CookieValue(value = "refresh_token", required = false) String refreshToken,
      HttpServletResponse response) {

    LogoutResult result = authFacade.handleLogout(refreshToken);

    if (result.isClearAccessToken()) {
      cookieService.clearAccessCookie(response, appConfiguration.isSecureCookie());
    }

    if (result.isClearRefreshToken()) {
      cookieService.clearRefreshCookie(response, appConfiguration.isSecureCookie());
    }

    return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.LOGOUT_SUCCESS, null));
  }
}
