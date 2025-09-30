package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginResponse;
import dev.roland.inventory_management_backend.dto.mail.EmailDetails;
import dev.roland.inventory_management_backend.model.OneTimeCode;
import dev.roland.inventory_management_backend.model.RefreshToken;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.model.enums.MailTemplate;
import dev.roland.inventory_management_backend.repository.RefreshTokenRepository;
import dev.roland.inventory_management_backend.security.JwtUtil;
import dev.roland.inventory_management_backend.service.EmailService;
import dev.roland.inventory_management_backend.service.OneTimeCodeService;
import dev.roland.inventory_management_backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthFacadeImpl  implements AuthFacade {

    private final UserService userService;
    private final EmailService emailService;
    private final OneTimeCodeService oneTimeCodeService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public ResponseEntity<Void> handleFirstLogin(EmailRequest request) {
        Optional<User> optionalUser = userService.findUserByEmail(request.getEmail());

        if (optionalUser.isEmpty() || optionalUser.get().getPassword() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            String generatedCode = String.valueOf(UUID.randomUUID());

            Map<String, Object> model = Map.of(
                    "username", optionalUser.get().getUsername(),
                    "oneTimeCode", generatedCode
            );

            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(request.getEmail())
                    .templateName(MailTemplate.ONE_TIME_CODE_MAIL)
                    .templateModel(model)
                    .build();

            boolean isEmailSent = emailService.sendMailWithTemplate(emailDetails);

            if (isEmailSent) {
                OneTimeCode oneTimeCode = OneTimeCode.builder()
                        .code(generatedCode)
                        .user(optionalUser.get())
                        .expiresAt(LocalDateTime.now().plusMinutes(30))
                        .build();

                oneTimeCodeService.save(oneTimeCode);
                return ResponseEntity.status(HttpStatus.OK).build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<LoginResponse> handleLogin(LoginRequest request) {
        Optional<User> optionalUser = userService.findUserByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = optionalUser.get();

        try {
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword());
            authenticationManager.authenticate(authInputToken);

            LoginResponse.UserDetails userDetails = new LoginResponse.UserDetails(
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole()
            );

            return ResponseEntity.ok(new LoginResponse(userDetails, generateTokens(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    private LoginResponse.TokensDetails generateTokens(User user) {
        String refreshToken = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateToken(user);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setUser(user);
        tokenEntity.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(tokenEntity);

        return new LoginResponse.TokensDetails(accessToken, refreshToken);
    }
}
