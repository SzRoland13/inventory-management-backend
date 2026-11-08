package dev.roland.inventory_management_backend.features.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.features.auth.CustomUserDetails;
import dev.roland.inventory_management_backend.features.auth.dto.PasswordSetupRequest;
import dev.roland.inventory_management_backend.features.auth.message.AuthMessageKey;
import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCode;
import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCodeTokens;
import dev.roland.inventory_management_backend.features.one_time_code.service.OneTimeCodeService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private UserService userService;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private OneTimeCodeService oneTimeCodeService;

  private AuthServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new AuthServiceImpl(userService, passwordEncoder, oneTimeCodeService);
  }

  @Test
  void setsPasswordOnlyForAnUninitializedAccountWithMatchingConfirmation() {
    final User user = User.builder().email("new@example.com").build();
    final OneTimeCode code =
        OneTimeCode.builder()
            .user(user)
            .codeHash(OneTimeCodeTokens.hash("setup-token"))
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .build();
    when(oneTimeCodeService.findByCodeHashForUpdate(OneTimeCodeTokens.hash("setup-token")))
        .thenReturn(java.util.Optional.of(code));
    when(passwordEncoder.encode("Correct!42")).thenReturn("encoded");
    final PasswordSetupRequest request = passwordRequest("Correct!42", "Correct!42");

    service.handleSetupOfNewPassword(request);

    assertEquals("encoded", user.getPassword());
    assertEquals(true, user.isOtcSetupComplete());
    verify(userService).save(user);
  }

  @Test
  void rejectsMismatchedOrAlreadyCompletedPasswordSetup() {
    final User existingUser = User.builder().email("new@example.com").password("hash").build();
    final OneTimeCode code =
        OneTimeCode.builder()
            .user(existingUser)
            .codeHash(OneTimeCodeTokens.hash("setup-token"))
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .build();
    when(oneTimeCodeService.findByCodeHashForUpdate(OneTimeCodeTokens.hash("setup-token")))
        .thenReturn(java.util.Optional.of(code));

    final ApiException mismatch =
        assertThrows(
            ApiException.class,
            () -> service.handleSetupOfNewPassword(passwordRequest("Correct!42", "Other!42")));
    assertEquals(AuthMessageKey.PASSWORDS_NOT_MATCH, mismatch.getMessageKey());

    final ApiException alreadySetup =
        assertThrows(
            ApiException.class,
            () -> service.handleSetupOfNewPassword(passwordRequest("Correct!42", "Correct!42")));
    assertEquals(AuthMessageKey.NOT_FIRST_LOGIN, alreadySetup.getMessageKey());
    verify(userService, never()).save(ArgumentMatchers.any(User.class));
  }

  @Test
  void sessionRequiresAuthenticatedPrincipalAndReturnsCurrentUser() {
    assertThrows(UnauthorizedException.class, () -> service.checkSession(null));
    final TestingAuthenticationToken unauthenticated = new TestingAuthenticationToken("x", "y");
    assertThrows(UnauthorizedException.class, () -> service.checkSession(unauthenticated));

    final CustomUserDetails principal =
        new CustomUserDetails(8L, "alice", "hash", UserRole.ADMIN, UserStatus.ACTIVE);
    final TestingAuthenticationToken authenticated =
        new TestingAuthenticationToken(principal, null, principal.getAuthorities());
    when(userService.findByIdOrThrow(8L))
        .thenReturn(
            User.builder()
                .id(8L)
                .username("alice")
                .email("alice@example.com")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

    assertEquals("alice", service.checkSession(authenticated).getUsername());
  }

  private PasswordSetupRequest passwordRequest(final String password, final String repeated) {
    final PasswordSetupRequest request = new PasswordSetupRequest();
    request.setOneTimeCode("setup-token");
    request.setPassword(password);
    request.setRepeatPassword(repeated);
    return request;
  }
}
