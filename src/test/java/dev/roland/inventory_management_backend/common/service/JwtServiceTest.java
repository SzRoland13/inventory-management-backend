package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

  private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

  @Mock private AppConfiguration configuration;

  private JwtService service;

  @BeforeEach
  void setUp() {
    when(configuration.getSecret()).thenReturn(SECRET);
    service = new JwtService(configuration);
  }

  @Test
  void createsAccessTokenWithIdentityAndRoleClaims() {
    when(configuration.getAccessTokenExpirationTime()).thenReturn(60_000L);
    final User user =
        User.builder().username("alice").email("alice@example.com").role(UserRole.ADMIN).build();

    final String token = service.generateAccessToken(user);

    assertEquals("alice", service.extractUsername(token));
    assertEquals("ADMIN", service.extractRole(token));
    assertTrue(service.isTokenValid(token, user));
    final UserDetails matchingDetails =
        org.springframework.security.core.userdetails.User.withUsername("alice")
            .password("ignored")
            .authorities("ROLE_ADMIN")
            .build();
    final UserDetails otherDetails =
        org.springframework.security.core.userdetails.User.withUsername("bob")
            .password("ignored")
            .authorities("ROLE_ADMIN")
            .build();
    assertTrue(service.isTokenValid(token, matchingDetails));
    assertFalse(service.isTokenValid(token, otherDetails));
  }

  @Test
  void refreshTokenCarriesSubjectAndCallerSuppliedClaims() {
    when(configuration.getRefreshTokenExpirationTime()).thenReturn(120_000L);
    final User user = User.builder().username("alice").build();

    final String token = service.generateRefreshToken(Map.of("scope", "inventory"), user);

    assertEquals("alice", service.extractUsername(token));
    assertEquals(
        "inventory", service.extractClaim(token, claims -> claims.get("scope", String.class)));
    assertFalse(service.isTokenExpired(token));
  }
}
