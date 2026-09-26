package dev.roland.inventory_management_backend.features.auth.facade.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.common.dto.mail.EmailDetails;
import dev.roland.inventory_management_backend.common.enumeration.MailTemplate;
import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.common.service.EmailService;
import dev.roland.inventory_management_backend.common.service.JwtService;
import dev.roland.inventory_management_backend.common.service.LoginSessionService;
import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.common.service.TwoFactorAuthService;
import dev.roland.inventory_management_backend.features.auth.dto.AuthTokens;
import dev.roland.inventory_management_backend.features.auth.dto.EmailRequest;
import dev.roland.inventory_management_backend.features.auth.dto.FirstLoginValidationRequest;
import dev.roland.inventory_management_backend.features.auth.dto.LoginFinalizationResult;
import dev.roland.inventory_management_backend.features.auth.dto.LoginRequest;
import dev.roland.inventory_management_backend.features.auth.dto.LoginResponse;
import dev.roland.inventory_management_backend.features.auth.dto.LogoutResult;
import dev.roland.inventory_management_backend.features.auth.dto.ShortLifeTokenResponse;
import dev.roland.inventory_management_backend.features.auth.dto.TokenRefreshResult;
import dev.roland.inventory_management_backend.features.auth.dto.TokenWithExpiry;
import dev.roland.inventory_management_backend.features.auth.dto.TwoFactorVerifyRequest;
import dev.roland.inventory_management_backend.features.auth.facade.AuthFacade;
import dev.roland.inventory_management_backend.features.auth.message.AuthMessageKey;
import dev.roland.inventory_management_backend.features.media_asset.dto.PresignedUrlData;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCode;
import dev.roland.inventory_management_backend.features.one_time_code.service.OneTimeCodeService;
import dev.roland.inventory_management_backend.features.refresh_token.RefreshToken;
import dev.roland.inventory_management_backend.features.refresh_token.service.RefreshTokenService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

/** Coordinates login, one-time-code, token, and two-factor authentication workflows. */
@Service
@RequiredArgsConstructor
public class AuthFacadeImpl implements AuthFacade {

  private final UserService userService;
  private final EmailService emailService;
  private final OneTimeCodeService oneTimeCodeService;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;
  private final TwoFactorAuthService twoFactorAuthService;
  private final LoginSessionService loginSessionService;
  private final AppConfiguration appConfiguration;
  private final MediaUsageService mediaUsageService;
  private final ObjectStorageService objectStorageService;

  /** {@inheritDoc} */
  @Transactional
  @Override
  public void sendOneTimeCode(final EmailRequest request) {
    final User user = userService.findUserByEmailOrThrow(request.getEmail());

    if (user.getPassword() != null) {
      throw new ApiException(AuthMessageKey.NOT_FIRST_LOGIN);
    }

    final OneTimeCode code =
        oneTimeCodeService.findByUserId(user.getId()).orElse(new OneTimeCode());

    code.setUser(user);
    code.setCode(generateOneTimeCode());
    code.setExpiresAt(LocalDateTime.now().plusMinutes(30));

    if (!sendFirstLoginEmail(user, code.getCode())) {
      throw new ApiException(AuthMessageKey.EMAIL_SEND_FAILED);
    } else {
      oneTimeCodeService.save(code);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void validateOneTimeCode(final FirstLoginValidationRequest request) {
    final OneTimeCode oneTimeCode =
        oneTimeCodeService
            .findByCode(request.getOneTimeCode())
            .orElseThrow(() -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS));

    final User user = oneTimeCode.getUser();

    if (user == null || !user.getEmail().equals(request.getEmail())) {
      throw new ApiException(AuthMessageKey.INVALID_CREDENTIALS);
    }

    if (oneTimeCode.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new ApiException(AuthMessageKey.ONE_TIME_CODE_EXPIRED);
    }

    oneTimeCodeService.delete(oneTimeCode);
  }

  /**
   * Generates a random one-time code for login verification.
   *
   * @return generated one-time code
   */
  private String generateOneTimeCode() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * Sends the one-time code email to the user.
   *
   * @param user account receiving the code
   * @param code one-time code included in the email
   * @return true when the email was sent successfully
   */
  private boolean sendFirstLoginEmail(final User user, final String code) {
    final Map<String, Object> model = Map.of("username", user.getUsername(), "oneTimeCode", code);

    final EmailDetails emailDetails =
        EmailDetails.builder()
            .recipient(user.getEmail())
            .templateName(MailTemplate.ONE_TIME_CODE_MAIL)
            .subject("One Time Code for Login")
            .templateModel(model)
            .build();

    return emailService.sendMailWithTemplate(emailDetails);
  }

  /** {@inheritDoc} */
  @Override
  public ShortLifeTokenResponse handleLogin(final LoginRequest request) {
    final User user = userService.findUserByEmailOrThrow(request.getEmail());

    try {
      final UsernamePasswordAuthenticationToken authInputToken =
          new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword());
      authenticationManager.authenticate(authInputToken);
    } catch (BadCredentialsException e) {
      throw new ApiException(AuthMessageKey.INVALID_CREDENTIALS);
    }

    final TokenWithExpiry tokenWithExpiry =
        loginSessionService.createTemporarySessionWithExpiry(user.getEmail());

    return ShortLifeTokenResponse.builder()
        .twoFactorEnabled(user.is2faEnabled())
        .shortLifeToken(tokenWithExpiry.getToken())
        .expiresAt(tokenWithExpiry.getExpiresAt())
        .build();
  }

  /** {@inheritDoc} */
  @Override
  public TokenRefreshResult handleTokenRefresh(final String token) {

    if (token == null) {
      throw new UnauthorizedException(AuthMessageKey.INVALID_CREDENTIALS);
    }

    final RefreshToken savedToken =
        refreshTokenService
            .findByToken(token)
            .orElseThrow(() -> new UnauthorizedException(AuthMessageKey.INVALID_CREDENTIALS));

    final TokenRefreshResult result;
    if (jwtService.isTokenExpired(savedToken.getToken())) {
      refreshTokenService.delete(savedToken);
      result = new TokenRefreshResult(null, true);
    } else {
      final String newAccessToken = jwtService.generateAccessToken(savedToken.getUser());
      result = new TokenRefreshResult(newAccessToken, false);
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public String setup2fa(final EmailRequest request) {
    final User user = userService.findUserByEmailOrThrow(request.getEmail());

    if (user.is2faEnabled()) {
      throw new ApiException(AuthMessageKey.TWO_FA_ALREADY_ENABLED);
    }

    if (!user.isOtcSetupComplete()) {
      throw new ApiException(AuthMessageKey.ONE_TIME_CODE_SHOULD_BE_VERIFIED_FIRST);
    }

    final String secret = twoFactorAuthService.generateSecret();

    user.setTotpSecret(secret);
    userService.save(user);

    return twoFactorAuthService.generateQrCodeImage(secret, user.getEmail());
  }

  /**
   * {@inheritDoc}
   *
   * @param request request supplied to this method
   * @return verify2fa login result
   */
  @Override
  @Transactional
  public LoginFinalizationResult verify2faLogin(final TwoFactorVerifyRequest request) {

    boolean firstTimeTwoFaEnabled = false;

    final User user = userService.findUserByEmailOrThrow(request.getEmail());

    final String emailAddressFromToken =
        loginSessionService.consumeSessionToken(request.getShortLifeToken());

    if (emailAddressFromToken == null || !emailAddressFromToken.equals(request.getEmail())) {
      throw new ApiException(AuthMessageKey.INVALID_OR_EXPIRED_SESSION);
    }

    final boolean valid = twoFactorAuthService.verifyCode(user.getTotpSecret(), request.getCode());
    if (!valid) {
      throw new ApiException(AuthMessageKey.INVALID_TWO_FA_CODE);
    }

    if (!user.is2faEnabled() && user.isOtcSetupComplete()) {
      user.set2faEnabled(true);
      user.setStatus(UserStatus.ACTIVE);
      userService.save(user);
      firstTimeTwoFaEnabled = true;
    }

    final AuthTokens tokens = generateTokens(user);

    final LoginResponse.UserDetails userDetails =
        LoginResponse.UserDetails.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .build();

    final Optional<MediaUsage> mediaUsage =
        mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.USER, user.getId(), MediaUsageType.AVATAR);

    if (mediaUsage.isPresent()) {
      final PresignedUrlData imageData =
          objectStorageService.generatePresignedGetUrl(
              mediaUsage.get().getMediaAsset().getObjectPath());

      userDetails.setAvatarId(mediaUsage.get().getMediaAsset().getId());
      userDetails.setAvatarUrl(imageData.getUrl());
      userDetails.setAvatarUrlExpiry(imageData.getExpiry());
    }

    final LoginResponse response = new LoginResponse(userDetails, firstTimeTwoFaEnabled);

    loginSessionService.deleteToken(request.getShortLifeToken());

    return new LoginFinalizationResult(response, tokens, firstTimeTwoFaEnabled);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public LogoutResult handleLogout(final String refreshToken) {

    if (refreshToken != null) {
      refreshTokenService.findByToken(refreshToken).ifPresent(refreshTokenService::delete);
    }

    return new LogoutResult(true, true);
  }

  /**
   * {@inheritDoc}
   *
   * @param user user supplied to this method
   * @return generate tokens result
   */
  @Transactional
  private AuthTokens generateTokens(final User user) {
    final String accessToken = jwtService.generateAccessToken(user);
    final String refreshToken = jwtService.generateRefreshToken(user);

    final RefreshToken tokenEntity = new RefreshToken();
    tokenEntity.setToken(refreshToken);
    tokenEntity.setUser(user);
    tokenEntity.setExpiryDate(
        LocalDateTime.now().plusSeconds(appConfiguration.getRefreshTokenExpirationTime() / 1000));
    refreshTokenService.save(tokenEntity);

    return new AuthTokens(accessToken, refreshToken);
  }
}
