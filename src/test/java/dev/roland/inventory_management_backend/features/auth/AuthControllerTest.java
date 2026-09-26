package dev.roland.inventory_management_backend.features.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.common.service.HttpOnlyCookieService;
import dev.roland.inventory_management_backend.features.auth.dto.AuthTokens;
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
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.dto.UserDto;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;
  @Mock private AuthFacade authFacade;
  @Mock private HttpOnlyCookieService cookieService;
  @Mock private AppConfiguration appConfiguration;

  private AuthController controller;

  @BeforeEach
  void setUp() {
    controller = new AuthController(authService, authFacade, cookieService, appConfiguration);
  }

  @Test
  void firstLoginEndpointUsesTheMatchingMessageAndDelegatesRequest() {
    final EmailRequest request = email("alice@example.com");
    when(authService.checkIfFirstLogin(request))
        .thenReturn(
            new CheckFirstLoginResponse(true, true), new CheckFirstLoginResponse(true, false));

    final var firstLogin = controller.checkIfFirstLogin(request);
    final var returningLogin = controller.checkIfFirstLogin(request);

    assertEquals(200, firstLogin.getStatusCode().value());
    assertEquals(AuthMessageKey.FIRST_LOGIN.getKey(), firstLogin.getBody().getMessageKey());
    assertEquals(AuthMessageKey.NOT_FIRST_LOGIN.getKey(), returningLogin.getBody().getMessageKey());
  }

  @Test
  void onboardingAndLoginEndpointsReturnSuccessfulResponses() {
    final EmailRequest email = email("alice@example.com");
    final FirstLoginValidationRequest validation = new FirstLoginValidationRequest();
    validation.setEmail("alice@example.com");
    validation.setOneTimeCode("123");
    final PasswordSetupRequest password = new PasswordSetupRequest();
    password.setEmail("alice@example.com");
    final LoginRequest login = new LoginRequest();
    login.setEmail("alice@example.com");
    login.setPassword("secret");
    when(authFacade.handleLogin(login))
        .thenReturn(ShortLifeTokenResponse.builder().shortLifeToken("short").build());

    assertEquals(201, controller.sendOneTimeCode(email).getStatusCode().value());
    assertEquals(200, controller.validateOneTimeCode(validation).getStatusCode().value());
    assertEquals(200, controller.handleSetupOfNewPassword(password).getStatusCode().value());
    assertEquals("short", controller.handleLogin(login).getBody().getPayload().getShortLifeToken());
    verify(authFacade).sendOneTimeCode(email);
    verify(authFacade).validateOneTimeCode(validation);
    verify(authService).handleSetupOfNewPassword(password);
  }

  @Test
  void refreshEndpointSetsAccessCookieOrClearsExpiredRefreshCookie() {
    final MockHttpServletResponse response = new MockHttpServletResponse();
    when(appConfiguration.isSecureCookie()).thenReturn(true);
    when(appConfiguration.getAccessTokenExpirationTime()).thenReturn(60_000L);
    when(authFacade.handleTokenRefresh("refresh"))
        .thenReturn(new TokenRefreshResult("access", false));

    assertEquals(200, controller.handleTokenRefresh("refresh", response).getStatusCode().value());
    verify(cookieService).setAccessCookie(response, "access", true, 60);

    when(authFacade.handleTokenRefresh("expired")).thenReturn(new TokenRefreshResult(null, true));
    final var expired = controller.handleTokenRefresh("expired", response);
    assertEquals(401, expired.getStatusCode().value());
    verify(cookieService).clearRefreshCookie(response, true);
  }

  @Test
  void twoFactorSetupAndLoginSetAuthenticationCookies() {
    final EmailRequest email = email("alice@example.com");
    when(authFacade.setup2fa(email)).thenReturn("qr-code");
    assertEquals("qr-code", controller.setup2fa(email).getBody().getPayload());

    when(appConfiguration.isSecureCookie()).thenReturn(false);
    when(appConfiguration.getAccessTokenExpirationTime()).thenReturn(120_000L);
    when(appConfiguration.getRefreshTokenExpirationTime()).thenReturn(600_000L);
    final LoginResponse loginResponse =
        new LoginResponse(
            LoginResponse.UserDetails.builder().id(3L).email("alice@example.com").build(), true);
    when(authFacade.verify2faLogin(any(TwoFactorVerifyRequest.class)))
        .thenReturn(
            new LoginFinalizationResult(loginResponse, new AuthTokens("access", "refresh"), true));
    final MockHttpServletResponse response = new MockHttpServletResponse();

    final var result = controller.verify2faLogin(new TwoFactorVerifyRequest(), response);

    assertEquals(200, result.getStatusCode().value());
    assertEquals(AuthMessageKey.TWO_FA_SETUP_COMPLETE.getKey(), result.getBody().getMessageKey());
    verify(cookieService).setAccessCookie(response, "access", false, 120);
    verify(cookieService).setRefreshCookie(response, "refresh", false, 600);
  }

  @Test
  void checkSessionDelegatesAndReturnsUser() {
    final User user =
        User.builder()
            .id(3L)
            .username("alice")
            .email("alice@example.com")
            .role(UserRole.ADMIN)
            .status(UserStatus.ACTIVE)
            .build();
    when(authService.checkSession(null)).thenReturn(new UserDto(user));

    final var response = controller.checkSession(null);

    assertEquals("alice", response.getBody().getPayload().getUsername());
  }

  @Test
  void logoutClearsOnlyTheCookiesRequestedByTheFacade() {
    final MockHttpServletResponse response = new MockHttpServletResponse();
    when(appConfiguration.isSecureCookie()).thenReturn(true);
    when(authFacade.handleLogout("refresh")).thenReturn(new LogoutResult(true, true));

    assertEquals(200, controller.handleLogout("refresh", response).getStatusCode().value());

    verify(cookieService).clearAccessCookie(response, true);
    verify(cookieService).clearRefreshCookie(response, true);
  }

  private EmailRequest email(final String address) {
    final EmailRequest request = new EmailRequest();
    request.setEmail(address);
    return request;
  }
}
