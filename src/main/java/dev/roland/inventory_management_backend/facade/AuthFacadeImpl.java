package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.*;
import dev.roland.inventory_management_backend.dto.mail.EmailDetails;
import dev.roland.inventory_management_backend.messageKey.ApiException;
import dev.roland.inventory_management_backend.messageKey.AuthMessageKey;
import dev.roland.inventory_management_backend.model.OneTimeCode;
import dev.roland.inventory_management_backend.model.RefreshToken;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.model.enums.MailTemplate;
import dev.roland.inventory_management_backend.security.JwtUtil;
import dev.roland.inventory_management_backend.service.*;
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
public class AuthFacadeImpl  implements AuthFacade {

    private final UserService userService;
    private final EmailService emailService;
    private final OneTimeCodeService oneTimeCodeService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final LoginSessionService loginSessionService;

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
        User user = userService.findUserByEmail(request.getEmail()).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        if (user.getPassword() != null) {
            throw new ApiException(AuthMessageKey.NOT_FIRST_LOGIN);
        }

        String oneTimeCode = generateOneTimeCode();

        if (sendFirstLoginEmail(user, oneTimeCode)) {
            oneTimeCodeService.save(
                    OneTimeCode.builder()
                            .code(oneTimeCode)
                            .user(user)
                            .expiresAt(LocalDateTime.now().plusMinutes(30))
                            .build()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(AuthMessageKey.LOGIN_SUCCESS, null));
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
        User user = userService.findUserByEmail(request.getEmail()).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        try {
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword());
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            throw new ApiException(AuthMessageKey.INVALID_CREDENTIALS);
        }

        TokenWithExpiry tokenWithExpiry = loginSessionService.createTemporarySessionWithExpiry(user.getEmail());

        ShortLifeTokenResponse response = ShortLifeTokenResponse
                .builder()
                .shortLifeToken(tokenWithExpiry.getToken())
                .expiresAt(tokenWithExpiry.getExpiresAt())
                .build();

        return !user.is2faEnabled() ?
                ResponseEntity.badRequest().body(ApiResponse.failure(AuthMessageKey.TWO_FA_NOT_ENABLED, response))
                : ResponseEntity.ok(ApiResponse.success(AuthMessageKey.PASSWORD_VALID_NEEDS_TWO_FA, response));
    }


    /**
     * Handles refresh token validation and access token regeneration.
     *
     * @param request contains refresh token
     * @return new access and the provided refresh token
     * @throws ApiException if refresh token invalid or expired
     */
    @Override
    public ResponseEntity<ApiResponse<LoginResponse.TokensDetails>> handleTokenRefresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        RefreshToken tokenEntity = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new ApiException(AuthMessageKey.INVALID_TOKEN));

        if (tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenService.delete(tokenEntity);
            throw new ApiException(AuthMessageKey.TOKEN_EXPIRED);
        }

        User user = tokenEntity.getUser();
        if (user == null) {
            throw new ApiException(AuthMessageKey.INVALID_CREDENTIALS);
        }

        String newAccessToken = jwtUtil.generateToken(tokenEntity.getUser());
        return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.TOKEN_REFRESHED, new LoginResponse.TokensDetails(newAccessToken, refreshToken)));
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
        User user = userService.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS));

        if (user.is2faEnabled()) {
            throw new ApiException(AuthMessageKey.TWO_FA_ALREADY_ENABLED);
        }

        String secret = twoFactorAuthService.generateSecret();

        user.setTotpSecret(secret);
        userService.save(user);

        String qrCode = twoFactorAuthService.generateQrCodeImage(secret, user.getEmail());

        return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.TWO_FA_CODE_GENERATED, qrCode));
    }

    /**
     * Verifies the 2FA TOTP code provided by the user during setup.
     *
     * @param request contains the user's email and the TOTP verification code
     * @return ApiResponse indicates if the setup was successful
     * @throws ApiException if user not found or code is invalid
     */
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Void>> verify2fa(TwoFactorVerifyRequest request) {
        User user = userService.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS));

        boolean valid = twoFactorAuthService.verifyCode(user.getTotpSecret(), request.getCode());
        if (!valid) {
            throw new ApiException(AuthMessageKey.INVALID_TWO_FA_CODE);
        }

        user.set2faEnabled(true);
        userService.save(user);

        return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.TWO_FA_SETUP_COMPLETE, null));
    }

    /**
     * Verifies the 2FA TOTP code provided by the user during login.
     *
     * @param request contains the user's email and the TOTP verification code
     * @return ApiResponse user data and access tokens
     * @throws ApiException if user not found or code is invalid
     */
    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> verify2faLogin(TwoFactorVerifyRequest request) {
        User user = userService.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS));

        String emailFromToken = loginSessionService.consumeSessionToken(request.getShortLifeToken());

        if (emailFromToken == null || !emailFromToken.equals(request.getEmail())) {
            throw new ApiException(AuthMessageKey.INVALID_OR_EXPIRED_SESSION);
        }

        boolean valid = twoFactorAuthService.verifyCode(user.getTotpSecret(), request.getCode());
        if (!valid) {
            throw new ApiException(AuthMessageKey.INVALID_TWO_FA_CODE);
        }

        LoginResponse.UserDetails userDetails = new LoginResponse.UserDetails(
                user.getEmail(),
                user.getUsername(),
                user.getRole()
        );

        return ResponseEntity.ok(
                ApiResponse.success(AuthMessageKey.LOGIN_SUCCESS, new LoginResponse(userDetails, generateTokens(user)))
        );
    }

    /**
     * Generates new access and refresh tokens for the given user.
     *
     * @param user {@link User} entity to generate tokens for
     * @return access and refresh tokens
     */
    @Transactional
    private LoginResponse.TokensDetails generateTokens(User user) {
        String refreshToken = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateToken(user);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setUser(user);
        tokenEntity.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenService.save(tokenEntity);

        return new LoginResponse.TokensDetails(accessToken, refreshToken);
    }
}
