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
import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.common.service.EmailService;
import dev.roland.inventory_management_backend.common.service.JwtService;
import dev.roland.inventory_management_backend.common.service.LoginSessionService;
import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.common.service.TwoFactorAuthService;
import dev.roland.inventory_management_backend.dto.auth.AuthTokens;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.FirstLoginValidationRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginFinalizationResult;
import dev.roland.inventory_management_backend.dto.auth.LoginRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginResponse;
import dev.roland.inventory_management_backend.dto.auth.LogoutResult;
import dev.roland.inventory_management_backend.dto.auth.ShortLifeTokenResponse;
import dev.roland.inventory_management_backend.dto.auth.TokenRefreshResult;
import dev.roland.inventory_management_backend.dto.auth.TokenWithExpiry;
import dev.roland.inventory_management_backend.dto.auth.TwoFactorVerifyRequest;
import dev.roland.inventory_management_backend.dto.mail.EmailDetails;
import dev.roland.inventory_management_backend.dto.media.PresignedUrlData;
import dev.roland.inventory_management_backend.enums.MailTemplate;
import dev.roland.inventory_management_backend.enums.MediaEntityType;
import dev.roland.inventory_management_backend.enums.MediaUsageType;
import dev.roland.inventory_management_backend.enums.UserStatus;
import dev.roland.inventory_management_backend.features.auth.facade.AuthFacade;
import dev.roland.inventory_management_backend.features.auth.message.AuthMessageKey;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCode;
import dev.roland.inventory_management_backend.features.one_time_code.service.OneTimeCodeService;
import dev.roland.inventory_management_backend.features.refresh_token.RefreshToken;
import dev.roland.inventory_management_backend.features.refresh_token.service.RefreshTokenService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

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
  public void sendOneTimeCode(EmailRequest request) {
    User user = userService.findUserByEmailOrThrow(request.getEmail());

    if (user.getPassword() != null) {
      throw new ApiException(AuthMessageKey.NOT_FIRST_LOGIN);
    }

    OneTimeCode code = oneTimeCodeService.findByUserId(user.getId()).orElse(new OneTimeCode());

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
  public void validateOneTimeCode(FirstLoginValidationRequest request) {
    OneTimeCode oneTimeCode =
        oneTimeCodeService
            .findByCode(request.getOneTimeCode())
            .orElseThrow(() -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS));

    User user = oneTimeCode.getUser();

    if (user == null || !user.getEmail().equals(request.getEmail())) {
      throw new ApiException(AuthMessageKey.INVALID_CREDENTIALS);
    }

    if (oneTimeCode.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new ApiException(AuthMessageKey.ONE_TIME_CODE_EXPIRED);
    }

    oneTimeCodeService.delete(oneTimeCode);
  }

  /** Generates a random one-time code for login verification. */
  private String generateOneTimeCode() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /** Sends the one-time code email to the user. */
  private boolean sendFirstLoginEmail(User user, String code) {
    Map<String, Object> model = Map.of("username", user.getUsername(), "oneTimeCode", code);

    EmailDetails emailDetails =
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
  public ShortLifeTokenResponse handleLogin(LoginRequest request) {
    User user = userService.findUserByEmailOrThrow(request.getEmail());

    try {
      UsernamePasswordAuthenticationToken authInputToken =
          new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword());
      authenticationManager.authenticate(authInputToken);
    } catch (BadCredentialsException e) {
      throw new ApiException(AuthMessageKey.INVALID_CREDENTIALS);
    }

    TokenWithExpiry tokenWithExpiry =
        loginSessionService.createTemporarySessionWithExpiry(user.getEmail());

    return ShortLifeTokenResponse.builder()
        .twoFactorEnabled(user.is2faEnabled())
        .shortLifeToken(tokenWithExpiry.getToken())
        .expiresAt(tokenWithExpiry.getExpiresAt())
        .build();
  }

  /** {@inheritDoc} */
  @Override
  public TokenRefreshResult handleTokenRefresh(String token) {

    if (token == null) {
      throw new UnauthorizedException(AuthMessageKey.INVALID_CREDENTIALS);
    }

    RefreshToken savedToken =
        refreshTokenService
            .findByToken(token)
            .orElseThrow(() -> new UnauthorizedException(AuthMessageKey.INVALID_CREDENTIALS));

    if (jwtService.isTokenExpired(savedToken.getToken())) {
      refreshTokenService.delete(savedToken);
      return new TokenRefreshResult(null, true);
    }

    String newAccessToken = jwtService.generateAccessToken(savedToken.getUser());

    return new TokenRefreshResult(newAccessToken, false);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public String setup2fa(EmailRequest request) {
    User user = userService.findUserByEmailOrThrow(request.getEmail());

    if (user.is2faEnabled()) {
      throw new ApiException(AuthMessageKey.TWO_FA_ALREADY_ENABLED);
    }

    if (!user.isOtcSetupComplete()) {
      throw new ApiException(AuthMessageKey.ONE_TIME_CODE_SHOULD_BE_VERIFIED_FIRST);
    }

    String secret = twoFactorAuthService.generateSecret();

    user.setTotpSecret(secret);
    userService.save(user);

    return twoFactorAuthService.generateQrCodeImage(secret, user.getEmail());
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public LoginFinalizationResult verify2faLogin(TwoFactorVerifyRequest request) {

    boolean firstTime2FAEnabled = false;

    User user = userService.findUserByEmailOrThrow(request.getEmail());

    String emailAddressFromToken =
        loginSessionService.consumeSessionToken(request.getShortLifeToken());

    if (emailAddressFromToken == null || !emailAddressFromToken.equals(request.getEmail())) {
      throw new ApiException(AuthMessageKey.INVALID_OR_EXPIRED_SESSION);
    }

    boolean valid = twoFactorAuthService.verifyCode(user.getTotpSecret(), request.getCode());
    if (!valid) {
      throw new ApiException(AuthMessageKey.INVALID_TWO_FA_CODE);
    }

    if (!user.is2faEnabled() && user.isOtcSetupComplete()) {
      user.set2faEnabled(true);
      user.setStatus(UserStatus.ACTIVE);
      userService.save(user);
      firstTime2FAEnabled = true;
    }

    AuthTokens tokens = generateTokens(user);

    LoginResponse.UserDetails userDetails =
        LoginResponse.UserDetails.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .build();

    Optional<MediaUsage> mediaUsage =
        mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.USER, user.getId(), MediaUsageType.AVATAR);

    if (mediaUsage.isPresent()) {
      PresignedUrlData imageData =
          objectStorageService.generatePresignedGetUrl(
              mediaUsage.get().getMediaAsset().getObjectPath());

      userDetails.setAvatarId(mediaUsage.get().getMediaAsset().getId());
      userDetails.setAvatarUrl(imageData.getUrl());
      userDetails.setAvatarUrlExpiry(imageData.getExpiry());
    }

    LoginResponse response = new LoginResponse(userDetails, firstTime2FAEnabled);

    loginSessionService.deleteToken(request.getShortLifeToken());

    return new LoginFinalizationResult(response, tokens, firstTime2FAEnabled);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public LogoutResult handleLogout(String refreshToken) {

    if (refreshToken != null) {
      refreshTokenService.findByToken(refreshToken).ifPresent(refreshTokenService::delete);
    }

    return new LogoutResult(true, true);
  }

  /** {@inheritDoc} */
  @Transactional
  private AuthTokens generateTokens(User user) {
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    RefreshToken tokenEntity = new RefreshToken();
    tokenEntity.setToken(refreshToken);
    tokenEntity.setUser(user);
    tokenEntity.setExpiryDate(
        LocalDateTime.now().plusSeconds(appConfiguration.getRefreshTokenExpirationTime() / 1000));
    refreshTokenService.save(tokenEntity);

    return new AuthTokens(accessToken, refreshToken);
  }
}
