package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.CookieTokens;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.FirstLoginValidationRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginResponse;
import dev.roland.inventory_management_backend.dto.auth.ShortLifeTokenResponse;
import dev.roland.inventory_management_backend.dto.auth.TokenWithExpiry;
import dev.roland.inventory_management_backend.dto.auth.TwoFactorVerifyRequest;
import dev.roland.inventory_management_backend.dto.mail.EmailDetails;
import dev.roland.inventory_management_backend.enums.MailTemplate;
import dev.roland.inventory_management_backend.enums.UserStatus;
import dev.roland.inventory_management_backend.exception.ApiException;
import dev.roland.inventory_management_backend.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.messageKey.AuthMessageKey;
import dev.roland.inventory_management_backend.messageKey.MessageKey;
import dev.roland.inventory_management_backend.model.OneTimeCode;
import dev.roland.inventory_management_backend.model.RefreshToken;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.service.common.JwtService;
import dev.roland.inventory_management_backend.service.OneTimeCodeService;
import dev.roland.inventory_management_backend.service.RefreshTokenService;
import dev.roland.inventory_management_backend.service.UserService;
import dev.roland.inventory_management_backend.service.common.EmailService;
import dev.roland.inventory_management_backend.service.common.LoginSessionService;
import dev.roland.inventory_management_backend.service.common.TwoFactorAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * @return ApiResponse indicating whether the email was sent successfully
     * @throws ApiException if user does not exist or is not a first-time login
     */
    @Transactional
    @Override
    public ResponseEntity<ApiResponse<Void>> sendOneTimeCode(EmailRequest request) {
        User user = userService.findUserByEmailOrThrow(request.getEmail());

        if (user.getPassword() != null) {
            throw new ApiException(AuthMessageKey.NOT_FIRST_LOGIN);
        }

        OneTimeCode code = oneTimeCodeService.findByUserId(user.getId())
                .orElse(new OneTimeCode());

        code.setUser(user);
        code.setCode(generateOneTimeCode());
        code.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        if (sendFirstLoginEmail(user, code.getCode())) {
            oneTimeCodeService.save(code);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(AuthMessageKey.ONE_TIME_CODE_SENT, null));
        } else {
            throw new ApiException(AuthMessageKey.EMAIL_SEND_FAILED);
        }
    }

    /**
     * Validates user's first login one time code
     *
     * @param request user's email and one time code
     * @return ApiResponse indicating whether the one time code was valid
     * @throws ApiException if user does not exist or one time code invalid
     */
    @Override
    public ResponseEntity<ApiResponse<Void>> validateOneTimeCode(FirstLoginValidationRequest request) {
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
        return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.ONE_TIME_CODE_VALIDATION_SUCCESS, null));
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
    public ResponseEntity<ApiResponse<ShortLifeTokenResponse>> handleLogin(LoginRequest request) {
        User user = userService.findUserByEmailOrThrow(request.getEmail());

        try {
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword());
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            throw new ApiException(AuthMessageKey.INVALID_CREDENTIALS);
        }

        TokenWithExpiry tokenWithExpiry = loginSessionService.createTemporarySessionWithExpiry(user.getEmail());

        ShortLifeTokenResponse responseToken = ShortLifeTokenResponse
                .builder()
                .shortLifeToken(tokenWithExpiry.getToken())
                .expiresAt(tokenWithExpiry.getExpiresAt())
                .build();

        return !user.is2faEnabled() ?
                ResponseEntity.ok().body(ApiResponse.failure(AuthMessageKey.TWO_FA_NOT_ENABLED, responseToken))
                : ResponseEntity.accepted().body(ApiResponse.success(AuthMessageKey.PASSWORD_VALID_NEEDS_TWO_FA, responseToken));
    }

    /**
     * Handles refresh token validation and access token regeneration.
     *
     * @param token contains refresh token
     * @return new access and the provided refresh token
     * @throws ApiException if refresh token invalid or expired
     */
    @Override
    public ResponseEntity<ApiResponse<Void>> handleTokenRefresh(String token, HttpServletResponse response) {
        if (token == null) {
            throw new UnauthorizedException(AuthMessageKey.INVALID_CREDENTIALS);
        }

        RefreshToken savedToken =
                refreshTokenService
                        .findByToken(token)
                        .orElseThrow(() -> new UnauthorizedException(AuthMessageKey.INVALID_CREDENTIALS));

        if (jwtService.isTokenExpired(savedToken.getToken())) {
            refreshTokenService.delete(savedToken);

            // Clear the refresh token cookie
            Cookie refresh = new Cookie("refresh_token", "");
            refresh.setPath("/");
            refresh.setMaxAge(0);
            refresh.setHttpOnly(true);
            refresh.setSecure(appConfiguration.isSecureCookie());

            response.addCookie(refresh);

            throw new UnauthorizedException(AuthMessageKey.TOKEN_EXPIRED);
        }

        String newAccessToken = jwtService.generateAccessToken(savedToken.getUser());

        // Set new access token as HTTP-only cookie
        Cookie accessCookie = new Cookie("access_token", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(appConfiguration.isSecureCookie());
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (appConfiguration.getAccessTokenExpirationTime() / 1000));

        response.addCookie(accessCookie);

        return ResponseEntity.ok().build();
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
    public ResponseEntity<ApiResponse<String>> setup2fa(EmailRequest request) {
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

        String qrCode = twoFactorAuthService.generateQrCodeImage(secret, user.getEmail());

        return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.TWO_FA_CODE_GENERATED, qrCode));
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
    public ResponseEntity<ApiResponse<LoginResponse>> verify2faLogin(TwoFactorVerifyRequest request, HttpServletResponse response) {
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

        // If first-time setup, enable 2FA here
        if (!user.is2faEnabled() && user.isOtcSetupComplete()) {
            user.set2faEnabled(true);
            user.setStatus(UserStatus.ACTIVE);
            userService.save(user);
            firstTime2FAEnabled = true;
        }

        // Generate tokens and set as HTTP-only cookies
        CookieTokens tokens = generateTokens(user);
        setAuthCookies(response, tokens);

        LoginResponse.UserDetails userDetails = new LoginResponse.UserDetails(
                user.getEmail(),
                user.getUsername(),
                user.getRole()
        );

        MessageKey key = firstTime2FAEnabled ? AuthMessageKey.TWO_FA_SETUP_COMPLETE : AuthMessageKey.LOGIN_SUCCESS;

        return ResponseEntity.ok(
                ApiResponse.success(key, new LoginResponse(userDetails, firstTime2FAEnabled))
        );
    }

    /**
     * Handles user logout by clearing auth cookies.
     *
     * @param refreshToken the refresh token from cookie
     * @param response HttpServletResponse to clear cookies
     * @return success response
     */
    @Transactional
    @Override
    public ResponseEntity<ApiResponse<Void>> handleLogout(String refreshToken, HttpServletResponse response) {
        // Delete refresh token from database if exists
        if (refreshToken != null) {
            refreshTokenService.findByToken(refreshToken)
                    .ifPresent(refreshTokenService::delete);
        }

        // Clear both access and refresh token cookies
        clearAuthCookies(response);

        return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.LOGOUT_SUCCESS, null));
    }

    /**
     * Generates new access and refresh tokens for the given user.
     *
     * @param user {@link User} entity to generate tokens for
     * @return access and refresh tokens
     */
    @Transactional
    private CookieTokens generateTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setUser(user);
        tokenEntity.setExpiryDate(LocalDateTime.now().plusSeconds(appConfiguration.getRefreshTokenExpirationTime() / 1000));
        refreshTokenService.save(tokenEntity);

        return new CookieTokens(accessToken, refreshToken);
    }

    /**
     * Sets authentication cookies (access and refresh tokens) as HTTP-only cookies.
     *
     * @param response HttpServletResponse to add cookies
     * @param tokens CookieTokens containing access and refresh tokens
     */
    private void setAuthCookies(HttpServletResponse response, CookieTokens tokens) {
        // Set access token cookie
        Cookie accessCookie = new Cookie("access_token", tokens.getAccessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(appConfiguration.isSecureCookie());
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (appConfiguration.getAccessTokenExpirationTime() / 1000));
        response.addCookie(accessCookie);

        // Set refresh token cookie
        Cookie refreshCookie = new Cookie("refresh_token", tokens.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(appConfiguration.isSecureCookie());
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (appConfiguration.getRefreshTokenExpirationTime() / 1000));
        response.addCookie(refreshCookie);
    }

    /**
     * Clears authentication cookies by setting their max age to 0.
     *
     * @param response HttpServletResponse to clear cookies
     */
    private void clearAuthCookies(HttpServletResponse response) {
        // Clear access token cookie
        Cookie accessCookie = new Cookie("access_token", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(appConfiguration.isSecureCookie());
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        // Clear refresh token cookie
        Cookie refreshCookie = new Cookie("refresh_token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(appConfiguration.isSecureCookie());
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }
}