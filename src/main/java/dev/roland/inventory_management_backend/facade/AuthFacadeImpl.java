package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.configuration.AppConfiguration;
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
import dev.roland.inventory_management_backend.enums.MailTemplate;
import dev.roland.inventory_management_backend.enums.UserStatus;
import dev.roland.inventory_management_backend.exception.ApiException;
import dev.roland.inventory_management_backend.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.messageKey.AuthMessageKey;
import dev.roland.inventory_management_backend.model.OneTimeCode;
import dev.roland.inventory_management_backend.model.RefreshToken;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.service.OneTimeCodeService;
import dev.roland.inventory_management_backend.service.RefreshTokenService;
import dev.roland.inventory_management_backend.service.UserService;
import dev.roland.inventory_management_backend.service.common.EmailService;
import dev.roland.inventory_management_backend.service.common.JwtService;
import dev.roland.inventory_management_backend.service.common.LoginSessionService;
import dev.roland.inventory_management_backend.service.common.TwoFactorAuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

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

    /**
     *  Handles first-time login by verifying credentials and sending a one-time code.
     *
     * @param request user's email address
     * @throws ApiException if user does not exist or is not a first-time login
     */
    @Transactional
    @Override
    public void sendOneTimeCode(EmailRequest request) {
        User user = userService.findUserByEmailOrThrow(request.getEmail());

        if (user.getPassword() != null) {
            throw new ApiException(AuthMessageKey.NOT_FIRST_LOGIN);
        }

        OneTimeCode code = oneTimeCodeService.findByUserId(user.getId())
                .orElse(new OneTimeCode());

        code.setUser(user);
        code.setCode(generateOneTimeCode());
        code.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        if (!sendFirstLoginEmail(user, code.getCode())) {
            throw new ApiException(AuthMessageKey.EMAIL_SEND_FAILED);
        } else {
            oneTimeCodeService.save(code);
        }
    }

    /**
     * Validates user's first login one time code
     *
     * @param request user's email and one time code
     * @throws ApiException if user does not exist or one time code invalid
     */
    @Override
    public void validateOneTimeCode(FirstLoginValidationRequest request) {
        OneTimeCode oneTimeCode = oneTimeCodeService.findByCode(request.getOneTimeCode()).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        User user = oneTimeCode.getUser();

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
     */
    private String generateOneTimeCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Sends the one-time code email to the user.
     */
    private boolean sendFirstLoginEmail(User user, String code) {
        Map<String, Object> model = Map.of(
                "username", user.getUsername(),
                "oneTimeCode", code
        );

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(user.getEmail())
                .templateName(MailTemplate.ONE_TIME_CODE_MAIL)
                .subject("One Time Code for Login")
                .templateModel(model)
                .build();

        return emailService.sendMailWithTemplate(emailDetails);
    }

    /**
     * Handles login by verifying credentials and generating auth tokens
     *
     * @param request user's email address and password
     * @return void
     * @throws ApiException if user does not exist or provided credentials are invalid
     */
    @Override
    public ShortLifeTokenResponse handleLogin(LoginRequest request) {
        User user = userService.findUserByEmailOrThrow(request.getEmail());

        try {
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword());
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            throw new ApiException(AuthMessageKey.INVALID_CREDENTIALS);
        }

        TokenWithExpiry tokenWithExpiry = loginSessionService.createTemporarySessionWithExpiry(user.getEmail());

        return ShortLifeTokenResponse
                .builder()
                .shortLifeToken(tokenWithExpiry.getToken())
                .expiresAt(tokenWithExpiry.getExpiresAt())
                .build();
    }

    /**
     * Handles refresh token validation and access token regeneration.
     *
     * @param token contains refresh token
     * @return new access and the provided refresh token
     * @throws ApiException if refresh token invalid or expired
     */
    @Override
    public TokenRefreshResult handleTokenRefresh(String token) {

        if (token == null) {
            throw new UnauthorizedException(AuthMessageKey.INVALID_CREDENTIALS);
        }

        RefreshToken savedToken =
                refreshTokenService.findByToken(token)
                        .orElseThrow(() -> new UnauthorizedException(AuthMessageKey.INVALID_CREDENTIALS));

        if (jwtService.isTokenExpired(savedToken.getToken())) {
            refreshTokenService.delete(savedToken);
            return new TokenRefreshResult(null, true);
        }

        String newAccessToken = jwtService.generateAccessToken(savedToken.getUser());

        return new TokenRefreshResult(newAccessToken, false);
    }

    /**
     * Initializes Two-Factor Authentication (2FA) setup for a user.
     *
     * @param request contains the user's email
     * @return ApiResponse with the generated QR code image (Base64 data URI)
     * @throws ApiException if user does not exist or already has 2FA enabled
     */
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

    /**
     * Verifies the 2FA TOTP code provided by the user during login.
     * Sets HTTP-only cookies for access and refresh tokens.
     *
     * @param request contains the user's email and the TOTP verification code
     * @return ApiResponse user data and access tokens
     * @throws ApiException if user not found or code is invalid
     */
    @Override
    @Transactional
    public LoginFinalizationResult verify2faLogin(TwoFactorVerifyRequest request) {

        boolean firstTime2FAEnabled = false;

        User user = userService.findUserByEmailOrThrow(request.getEmail());

        String emailAddressFromToken = loginSessionService.consumeSessionToken(request.getShortLifeToken());

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
                new LoginResponse.UserDetails(
                        user.getEmail(),
                        user.getUsername(),
                        user.getRole()
                );

        LoginResponse response =
                new LoginResponse(userDetails, firstTime2FAEnabled);

        return new LoginFinalizationResult(
                response,
                tokens,
                firstTime2FAEnabled
        );
    }

    /**
     * Handles user logout by clearing auth cookies.
     *
     * @param refreshToken the refresh token from cookie
     * @return success response
     */
    @Override
    @Transactional
    public LogoutResult handleLogout(String refreshToken) {

        if (refreshToken != null) {
            refreshTokenService.findByToken(refreshToken)
                    .ifPresent(refreshTokenService::delete);
        }

        return new LogoutResult(true, true);
    }

    /**
     * Generates new access and refresh tokens for the given user.
     *
     * @param user {@link User} entity to generate tokens for
     * @return access and refresh tokens
     */
    @Transactional
    private AuthTokens generateTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setUser(user);
        tokenEntity.setExpiryDate(LocalDateTime.now().plusSeconds(appConfiguration.getRefreshTokenExpirationTime() / 1000));
        refreshTokenService.save(tokenEntity);

        return new AuthTokens(accessToken, refreshToken);
    }
}