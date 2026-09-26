package dev.roland.inventory_management_backend.features.auth;

import static dev.roland.inventory_management_backend.features.auth.AuthController.AUTH_BASE_ENDPOINT;

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

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.service.HttpOnlyCookieService;
import dev.roland.inventory_management_backend.features.auth.dto.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.features.auth.dto.EmailRequest;
import dev.roland.inventory_management_backend.features.auth.dto.FirstLoginValidationRequest;
import dev.roland.inventory_management_backend.features.auth.dto.LoginFinalizationResult;
import dev.roland.inventory_management_backend.features.auth.dto.LoginRequest;
import dev.roland.inventory_management_backend.features.auth.dto.LoginResponse;
import dev.roland.inventory_management_backend.features.auth.dto.LogoutResult;
import dev.roland.inventory_management_backend.features.auth.dto.PasswordSetupRequest;
import dev.roland.inventory_management_backend.features.auth.dto.ShortLifeTokenResponse;
import dev.roland.inventory_management_backend.features.auth.dto.TokenRefreshResult;
import dev.roland.inventory_management_backend.features.auth.dto.TwoFactorVerifyRequest;
import dev.roland.inventory_management_backend.features.auth.facade.AuthFacade;
import dev.roland.inventory_management_backend.features.auth.message.AuthMessageKey;
import dev.roland.inventory_management_backend.features.auth.service.AuthService;
import dev.roland.inventory_management_backend.features.user.dto.UserDto;
import lombok.RequiredArgsConstructor;

/** Exposes the authentication and token-management REST endpoints. */
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

  /**
   * Checks whether the email belongs to an account completing first login.
   *
   * @param request email to check
   * @return response indicating whether this is the account's first login
   */
  @PostMapping(CHECK_FIRST_LOGIN_ENDPOINT)
  ResponseEntity<ApiResponse<CheckFirstLoginResponse>> checkIfFirstLogin(
      @Valid @RequestBody final EmailRequest request) {
    final CheckFirstLoginResponse response = authService.checkIfFirstLogin(request);

    final AuthMessageKey messageKey =
        response.isFirstLogin() ? AuthMessageKey.FIRST_LOGIN : AuthMessageKey.NOT_FIRST_LOGIN;

    return ResponseEntity.ok(ApiResponse.success(messageKey, response));
  }

  /**
   * Sends a one-time code for first-login verification.
   *
   * @param request email address receiving the code
   * @return confirmation response
   */
  @PostMapping(SEND_OTC_ENDPOINT)
  ResponseEntity<ApiResponse<Void>> sendOneTimeCode(
      @Valid @RequestBody final EmailRequest request) {
    authFacade.sendOneTimeCode(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(AuthMessageKey.ONE_TIME_CODE_SENT, null));
  }

  /**
   * Validates the first-login one-time code.
   *
   * @param request email and one-time code to validate
   * @return validation response
   */
  @PostMapping(VALIDATE_OTC_ENDPOINT)
  ResponseEntity<ApiResponse<Void>> validateOneTimeCode(
      @Valid @RequestBody final FirstLoginValidationRequest request) {
    authFacade.validateOneTimeCode(request);

    return ResponseEntity.ok(
        ApiResponse.success(AuthMessageKey.ONE_TIME_CODE_VALIDATION_SUCCESS, null));
  }

  /**
   * Sets the password after first-login verification.
   *
   * @param request password setup details
   * @return confirmation response
   */
  @PostMapping(SETUP_PASSWORD_ENDPOINT)
  ResponseEntity<ApiResponse<Void>> handleSetupOfNewPassword(
      @Valid @RequestBody final PasswordSetupRequest request) {
    authService.handleSetupOfNewPassword(request);

    return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.PASSWORD_SETUP_SUCCESS, null));
  }

  /**
   * Starts authentication and returns the temporary login token.
   *
   * @param request login credentials
   * @return response containing the temporary login token
   */
  @PostMapping(LOGIN_ENDPOINT)
  ResponseEntity<ApiResponse<ShortLifeTokenResponse>> handleLogin(
      @Valid @RequestBody final LoginRequest request) {
    return ResponseEntity.ok()
        .body(
            ApiResponse.success(AuthMessageKey.VALID_CREDENTIALS, authFacade.handleLogin(request)));
  }

  /**
   * Refreshes the access token using the refresh cookie.
   *
   * @param refreshToken refresh token cookie, if present
   * @param response HTTP response receiving updated cookies
   * @return token refresh result
   */
  @PostMapping(REFRESH_ENDPOINT)
  ResponseEntity<ApiResponse<Void>> handleTokenRefresh(
      @CookieValue(value = "refresh_token", required = false) final String refreshToken,
      final HttpServletResponse response) {

    final TokenRefreshResult result = authFacade.handleTokenRefresh(refreshToken);

    if (result.isShouldClearRefreshToken()) {
      cookieService.clearRefreshCookie(response, appConfiguration.isSecureCookie());
    } else if (result.getAccessToken() != null) {
      cookieService.setAccessCookie(
          response,
          result.getAccessToken(),
          appConfiguration.isSecureCookie(),
          (int) appConfiguration.getAccessTokenExpirationTime() / 1000);
    }

    return result.isShouldClearRefreshToken()
        ? ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.failure(AuthMessageKey.TOKEN_EXPIRED))
        : ResponseEntity.ok(ApiResponse.success(AuthMessageKey.TOKEN_REFRESHED, null));
  }

  /**
   * Starts two-factor authentication setup for the account.
   *
   * @param request account email
   * @return response containing the authenticator setup data
   */
  @PostMapping(TWO_FA_SETUP_ENDPOINT)
  ResponseEntity<ApiResponse<String>> setup2fa(@Valid @RequestBody final EmailRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(AuthMessageKey.TWO_FA_CODE_GENERATED, authFacade.setup2fa(request)));
  }

  /**
   * Completes login with a two-factor verification code.
   *
   * @param request temporary token and verification code
   * @param response HTTP response receiving authentication cookies
   * @return response containing the signed-in user details
   */
  @PostMapping(TWO_FA_LOGIN_ENDPOINT)
  ResponseEntity<ApiResponse<LoginResponse>> verify2faLogin(
      @Valid @RequestBody final TwoFactorVerifyRequest request,
      final HttpServletResponse response) {

    final LoginFinalizationResult result = authFacade.verify2faLogin(request);

    cookieService.setAccessCookie(
        response,
        result.getTokens().getAccessToken(),
        appConfiguration.isSecureCookie(),
        (int) appConfiguration.getAccessTokenExpirationTime() / 1000);
    cookieService.setRefreshCookie(
        response,
        result.getTokens().getRefreshToken(),
        appConfiguration.isSecureCookie(),
        (int) appConfiguration.getRefreshTokenExpirationTime() / 1000);

    final MessageKey key =
        result.isFirstTime2faEnabled()
            ? AuthMessageKey.TWO_FA_SETUP_COMPLETE
            : AuthMessageKey.LOGIN_SUCCESS;

    return ResponseEntity.ok(ApiResponse.success(key, result.getLoginResponse()));
  }

  /**
   * Checks whether the current authenticated session is valid.
   *
   * @param authentication current authenticated principal
   * @return response containing the signed-in user details
   */
  @GetMapping(CHECK_SESSION_ENDPOINT)
  public ResponseEntity<ApiResponse<UserDto>> checkSession(final Authentication authentication) {
    return ResponseEntity.ok(
        ApiResponse.success(AuthMessageKey.TOKEN_VALID, authService.checkSession(authentication)));
  }

  /**
   * Ends the current session and clears authentication cookies.
   *
   * @param refreshToken refresh token cookie, if present
   * @param response HTTP response whose authentication cookies are cleared
   * @return logout confirmation response
   */
  @PostMapping(LOGOUT_ENDPOINT)
  ResponseEntity<ApiResponse<Void>> handleLogout(
      @CookieValue(value = "refresh_token", required = false) final String refreshToken,
      final HttpServletResponse response) {

    final LogoutResult result = authFacade.handleLogout(refreshToken);

    if (result.isClearAccessToken()) {
      cookieService.clearAccessCookie(response, appConfiguration.isSecureCookie());
    }

    if (result.isClearRefreshToken()) {
      cookieService.clearRefreshCookie(response, appConfiguration.isSecureCookie());
    }

    return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.LOGOUT_SUCCESS, null));
  }
}
