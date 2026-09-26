package dev.roland.inventory_management_backend.features.auth.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.common.service.EmailService;
import dev.roland.inventory_management_backend.common.service.JwtService;
import dev.roland.inventory_management_backend.common.service.LoginSessionService;
import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.common.service.TwoFactorAuthService;
import dev.roland.inventory_management_backend.features.auth.dto.EmailRequest;
import dev.roland.inventory_management_backend.features.auth.dto.FirstLoginValidationRequest;
import dev.roland.inventory_management_backend.features.auth.dto.LoginRequest;
import dev.roland.inventory_management_backend.features.auth.dto.TokenWithExpiry;
import dev.roland.inventory_management_backend.features.auth.dto.TwoFactorVerifyRequest;
import dev.roland.inventory_management_backend.features.auth.message.AuthMessageKey;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCode;
import dev.roland.inventory_management_backend.features.one_time_code.service.OneTimeCodeService;
import dev.roland.inventory_management_backend.features.refresh_token.RefreshToken;
import dev.roland.inventory_management_backend.features.refresh_token.service.RefreshTokenService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthFacadeImplTest {

  @Mock private UserService userService;
  @Mock private EmailService emailService;
  @Mock private OneTimeCodeService oneTimeCodeService;
  @Mock private JwtService jwtService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private TwoFactorAuthService twoFactorAuthService;
  @Mock private LoginSessionService loginSessionService;
  @Mock private AppConfiguration appConfiguration;
  @Mock private MediaUsageService mediaUsageService;
  @Mock private ObjectStorageService objectStorageService;

  private AuthFacadeImpl facade;

  @BeforeEach
  void setUp() {
    facade =
        new AuthFacadeImpl(
            userService,
            emailService,
            oneTimeCodeService,
            jwtService,
            authenticationManager,
            refreshTokenService,
            twoFactorAuthService,
            loginSessionService,
            appConfiguration,
            mediaUsageService,
            objectStorageService);
  }

  @Test
  void sendsAndPersistsOneTimeCodeForUserWithoutPassword() {
    final User user = User.builder().id(4L).username("alice").email("alice@example.com").build();
    when(userService.findUserByEmailOrThrow("alice@example.com")).thenReturn(user);
    when(oneTimeCodeService.findByUserId(4L)).thenReturn(Optional.empty());
    when(emailService.sendMailWithTemplate(any())).thenReturn(true);

    facade.sendOneTimeCode(email("alice@example.com"));

    verify(oneTimeCodeService)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                code ->
                    code.getUser() == user
                        && code.getCode().length() == 32
                        && code.getExpiresAt().isAfter(LocalDateTime.now())
                        && code.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(31))));
  }

  @Test
  void doesNotSendFirstLoginCodeForAccountWithPassword() {
    final User user = User.builder().password("hashed").email("alice@example.com").build();
    when(userService.findUserByEmailOrThrow("alice@example.com")).thenReturn(user);

    final ApiException exception =
        assertThrows(ApiException.class, () -> facade.sendOneTimeCode(email("alice@example.com")));

    assertEquals(AuthMessageKey.NOT_FIRST_LOGIN, exception.getMessageKey());
    verify(emailService, never()).sendMailWithTemplate(any());
    verify(oneTimeCodeService, never()).save(any());
  }

  @Test
  void reportsFailedEmailInsteadOfPersistingCode() {
    final User user = User.builder().id(4L).username("alice").email("alice@example.com").build();
    when(userService.findUserByEmailOrThrow("alice@example.com")).thenReturn(user);
    when(oneTimeCodeService.findByUserId(4L)).thenReturn(Optional.empty());
    when(emailService.sendMailWithTemplate(any())).thenReturn(false);

    final ApiException exception =
        assertThrows(ApiException.class, () -> facade.sendOneTimeCode(email("alice@example.com")));

    assertEquals(AuthMessageKey.EMAIL_SEND_FAILED, exception.getMessageKey());
    verify(oneTimeCodeService, never()).save(any());
  }

  @Test
  void validatesAndConsumesCodeForMatchingUnexpiredUser() {
    final User user = User.builder().email("alice@example.com").build();
    final OneTimeCode code =
        OneTimeCode.builder()
            .user(user)
            .code("one-time")
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .build();
    when(oneTimeCodeService.findByCode("one-time")).thenReturn(Optional.of(code));

    facade.validateOneTimeCode(validation("alice@example.com", "one-time"));

    verify(oneTimeCodeService).delete(code);
  }

  @Test
  void rejectsExpiredCodeAndDoesNotConsumeIt() {
    final OneTimeCode code =
        OneTimeCode.builder()
            .user(User.builder().email("alice@example.com").build())
            .code("expired")
            .expiresAt(LocalDateTime.now().minusSeconds(1))
            .build();
    when(oneTimeCodeService.findByCode("expired")).thenReturn(Optional.of(code));

    final ApiException exception =
        assertThrows(
            ApiException.class,
            () -> facade.validateOneTimeCode(validation("alice@example.com", "expired")));

    assertEquals(AuthMessageKey.ONE_TIME_CODE_EXPIRED, exception.getMessageKey());
    verify(oneTimeCodeService, never()).delete(code);
  }

  @Test
  void rejectsCodeForAnotherEmailAndUnknownCode() {
    final OneTimeCode code =
        OneTimeCode.builder()
            .user(User.builder().email("other@example.com").build())
            .code("code")
            .expiresAt(LocalDateTime.now().plusMinutes(1))
            .build();
    when(oneTimeCodeService.findByCode("code")).thenReturn(Optional.of(code));
    when(oneTimeCodeService.findByCode("missing")).thenReturn(Optional.empty());

    assertEquals(
        AuthMessageKey.INVALID_CREDENTIALS,
        assertThrows(
                ApiException.class,
                () -> facade.validateOneTimeCode(validation("alice@example.com", "code")))
            .getMessageKey());
    assertEquals(
        AuthMessageKey.INVALID_CREDENTIALS,
        assertThrows(
                ApiException.class,
                () -> facade.validateOneTimeCode(validation("alice@example.com", "missing")))
            .getMessageKey());
  }

  @Test
  void loginCreatesTemporarySessionAfterCredentialAuthentication() {
    final User user =
        User.builder().username("alice").email("alice@example.com").is2faEnabled(true).build();
    when(userService.findUserByEmailOrThrow("alice@example.com")).thenReturn(user);
    when(loginSessionService.createTemporarySessionWithExpiry("alice@example.com"))
        .thenReturn(new TokenWithExpiry("short-lived", java.time.Instant.now().plusSeconds(60)));
    final LoginRequest request = new LoginRequest();
    request.setEmail("alice@example.com");
    request.setPassword("password");

    final var result = facade.handleLogin(request);

    assertEquals("short-lived", result.getShortLifeToken());
    assertEquals(true, result.isTwoFactorEnabled());
  }

  @Test
  void loginMapsBadCredentialsToInvalidCredentials() {
    final User user = User.builder().username("alice").email("alice@example.com").build();
    when(userService.findUserByEmailOrThrow("alice@example.com")).thenReturn(user);
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("bad credentials"));
    final LoginRequest request = new LoginRequest();
    request.setEmail("alice@example.com");
    request.setPassword("wrong");

    final ApiException exception =
        assertThrows(ApiException.class, () -> facade.handleLogin(request));

    assertEquals(AuthMessageKey.INVALID_CREDENTIALS, exception.getMessageKey());
  }

  @Test
  void refreshRequiresKnownNonNullTokenAndDeletesExpiredTokens() {
    assertThrows(UnauthorizedException.class, () -> facade.handleTokenRefresh(null));
    when(refreshTokenService.findByToken("missing")).thenReturn(Optional.empty());
    assertThrows(UnauthorizedException.class, () -> facade.handleTokenRefresh("missing"));

    final RefreshToken token = RefreshToken.builder().token("expired").build();
    when(refreshTokenService.findByToken("expired")).thenReturn(Optional.of(token));
    when(jwtService.isTokenExpired("expired")).thenReturn(true);

    final var result = facade.handleTokenRefresh("expired");

    assertEquals(true, result.isShouldClearRefreshToken());
    verify(refreshTokenService).delete(token);
  }

  @Test
  void refreshCreatesAnAccessTokenForAnUnexpiredRefreshToken() {
    final User user = User.builder().username("alice").build();
    final RefreshToken token = RefreshToken.builder().token("valid").user(user).build();
    when(refreshTokenService.findByToken("valid")).thenReturn(Optional.of(token));
    when(jwtService.isTokenExpired("valid")).thenReturn(false);
    when(jwtService.generateAccessToken(user)).thenReturn("new-access");

    final var result = facade.handleTokenRefresh("valid");

    assertEquals("new-access", result.getAccessToken());
    assertEquals(false, result.isShouldClearRefreshToken());
  }

  @Test
  void setupTwoFactorRejectsAlreadyEnabledOrIncompleteAccountsAndPersistsSecret() {
    when(userService.findUserByEmailOrThrow("enabled@example.com"))
        .thenReturn(User.builder().is2faEnabled(true).build());
    assertEquals(
        AuthMessageKey.TWO_FA_ALREADY_ENABLED,
        assertThrows(ApiException.class, () -> facade.setup2fa(email("enabled@example.com")))
            .getMessageKey());

    when(userService.findUserByEmailOrThrow("incomplete@example.com"))
        .thenReturn(User.builder().isOtcSetupComplete(false).build());
    assertEquals(
        AuthMessageKey.ONE_TIME_CODE_SHOULD_BE_VERIFIED_FIRST,
        assertThrows(ApiException.class, () -> facade.setup2fa(email("incomplete@example.com")))
            .getMessageKey());

    final User user = User.builder().email("ready@example.com").isOtcSetupComplete(true).build();
    when(userService.findUserByEmailOrThrow("ready@example.com")).thenReturn(user);
    when(twoFactorAuthService.generateSecret()).thenReturn("totp-secret");
    when(twoFactorAuthService.generateQrCodeImage("totp-secret", "ready@example.com"))
        .thenReturn("qr-data");

    assertEquals("qr-data", facade.setup2fa(email("ready@example.com")));
    assertEquals("totp-secret", user.getTotpSecret());
    verify(userService).save(user);
  }

  @Test
  void twoFactorLoginRejectsInvalidSessionAndInvalidCode() {
    final User user = User.builder().email("alice@example.com").totpSecret("secret").build();
    when(userService.findUserByEmailOrThrow("alice@example.com")).thenReturn(user);
    when(loginSessionService.consumeSessionToken("expired")).thenReturn(null);
    when(loginSessionService.consumeSessionToken("valid-session")).thenReturn("alice@example.com");
    when(twoFactorAuthService.verifyCode("secret", "000000")).thenReturn(false);

    assertEquals(
        AuthMessageKey.INVALID_OR_EXPIRED_SESSION,
        assertThrows(
                ApiException.class, () -> facade.verify2faLogin(twoFactor("expired", "000000")))
            .getMessageKey());
    assertEquals(
        AuthMessageKey.INVALID_TWO_FA_CODE,
        assertThrows(
                ApiException.class,
                () -> facade.verify2faLogin(twoFactor("valid-session", "000000")))
            .getMessageKey());
  }

  @Test
  void firstSuccessfulTwoFactorLoginActivatesAccountAndIssuesTokens() {
    final User user =
        User.builder()
            .id(10L)
            .username("alice")
            .email("alice@example.com")
            .role(UserRole.ADMIN)
            .status(UserStatus.SETUP_REQUIRED)
            .totpSecret("totp-secret")
            .isOtcSetupComplete(true)
            .build();
    when(userService.findUserByEmailOrThrow("alice@example.com")).thenReturn(user);
    when(loginSessionService.consumeSessionToken("session")).thenReturn("alice@example.com");
    when(twoFactorAuthService.verifyCode("totp-secret", "123456")).thenReturn(true);
    when(jwtService.generateAccessToken(user)).thenReturn("access");
    when(jwtService.generateRefreshToken(user)).thenReturn("refresh");
    when(mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType
                .USER,
            10L,
            dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType
                .AVATAR))
        .thenReturn(Optional.empty());

    final var result = facade.verify2faLogin(twoFactor("session", "123456"));

    assertEquals(true, result.isFirstTime2faEnabled());
    assertEquals(UserStatus.ACTIVE, user.getStatus());
    assertEquals(true, user.is2faEnabled());
    assertEquals("access", result.getTokens().getAccessToken());
    assertEquals("refresh", result.getTokens().getRefreshToken());
    verify(refreshTokenService).save(any(RefreshToken.class));
    verify(loginSessionService).deleteToken("session");
  }

  @Test
  void logoutDeletesKnownRefreshTokenAndReturnsCookieClearFlags() {
    final RefreshToken token = RefreshToken.builder().token("refresh").build();
    when(refreshTokenService.findByToken("refresh")).thenReturn(Optional.of(token));

    final var result = facade.handleLogout("refresh");
    final var noTokenResult = facade.handleLogout(null);

    assertEquals(true, result.isClearAccessToken());
    assertEquals(true, result.isClearRefreshToken());
    assertEquals(true, noTokenResult.isClearAccessToken());
    verify(refreshTokenService).delete(token);
  }

  private EmailRequest email(final String address) {
    final EmailRequest request = new EmailRequest();
    request.setEmail(address);
    return request;
  }

  private FirstLoginValidationRequest validation(final String address, final String code) {
    final FirstLoginValidationRequest request = new FirstLoginValidationRequest();
    request.setEmail(address);
    request.setOneTimeCode(code);
    return request;
  }

  private TwoFactorVerifyRequest twoFactor(final String token, final String code) {
    final TwoFactorVerifyRequest request = new TwoFactorVerifyRequest();
    request.setEmail("alice@example.com");
    request.setCode(code);
    request.setShortLifeToken(token);
    return request;
  }
}
