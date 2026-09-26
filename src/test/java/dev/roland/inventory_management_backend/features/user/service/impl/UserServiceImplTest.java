package dev.roland.inventory_management_backend.features.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.dto.AddEditUserRequest;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.message.UserMessageKey;
import dev.roland.inventory_management_backend.features.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserRepository repository;

  private UserServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new UserServiceImpl(repository);
  }

  @Test
  void findsUsersByEmailAndUsernameOrThrowsWhenAbsent() {
    final User user = User.builder().id(3L).email("a@example.com").username("alice").build();
    when(repository.findByEmail("a@example.com")).thenReturn(Optional.of(user));
    when(repository.findByUsername("alice")).thenReturn(Optional.of(user));
    when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
    when(repository.findByUsername("missing")).thenReturn(Optional.empty());

    assertEquals(user, service.findUserByEmailOrThrow("a@example.com"));
    assertEquals(user, service.findByUsernameOrThrow("alice"));
    assertThrows(
        NotFoundException.class, () -> service.findUserByEmailOrThrow("missing@example.com"));
    assertThrows(NotFoundException.class, () -> service.findByUsernameOrThrow("missing"));
  }

  @Test
  void changingEmailClearsExistingAuthenticationSetup() {
    final User user =
        User.builder()
            .id(3L)
            .email("old@example.com")
            .username("alice")
            .password("hash")
            .totpSecret("secret")
            .is2faEnabled(true)
            .isOtcSetupComplete(true)
            .role(UserRole.ADMIN)
            .status(UserStatus.ACTIVE)
            .build();
    when(repository.findById(3L)).thenReturn(Optional.of(user));
    when(repository.save(user)).thenReturn(user);

    final var response = service.updateUser(3L, request("alice2", "new@example.com", "MANAGER"));

    assertEquals("new@example.com", user.getEmail());
    assertEquals("alice2", user.getUsername());
    assertEquals(UserRole.MANAGER, user.getRole());
    assertNull(user.getPassword());
    assertNull(user.getTotpSecret());
    assertEquals(false, user.is2faEnabled());
    assertEquals(false, user.isOtcSetupComplete());
    assertEquals("alice2", response.getUsername());
  }

  @Test
  void unchangedEmailPreservesAuthenticationAndUnknownRoleIsRejected() {
    final User user =
        User.builder()
            .id(3L)
            .email("same@example.com")
            .password("hash")
            .totpSecret("secret")
            .is2faEnabled(true)
            .isOtcSetupComplete(true)
            .role(UserRole.ADMIN)
            .status(UserStatus.ACTIVE)
            .build();
    when(repository.findById(3L)).thenReturn(Optional.of(user));

    final ApiException exception =
        assertThrows(
            ApiException.class,
            () -> service.updateUser(3L, request("alice", "same@example.com", "INVALID")));

    assertEquals("hash", user.getPassword());
    assertEquals("secret", user.getTotpSecret());
    assertEquals(true, user.is2faEnabled());
    assertEquals(true, user.isOtcSetupComplete());
    assertEquals(UserMessageKey.INVALID_ROLE, exception.getMessageKey());
  }

  private AddEditUserRequest request(final String username, final String email, final String role) {
    final AddEditUserRequest request = new AddEditUserRequest();
    request.setUsername(username);
    request.setEmail(email);
    request.setRole(role);
    return request;
  }
}
