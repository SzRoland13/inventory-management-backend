package dev.roland.inventory_management_backend.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import dev.roland.inventory_management_backend.common.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.common.service.CustomUserDetailsService;
import dev.roland.inventory_management_backend.common.service.HttpOnlyCookieService;
import dev.roland.inventory_management_backend.common.service.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

  @Mock private CustomUserDetailsService userDetailsService;
  @Mock private JwtService jwtService;
  @Mock private HttpOnlyCookieService cookieService;
  @Mock private FilterChain filterChain;

  private AuthenticationFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter = new AuthenticationFilter(userDetailsService, jwtService, cookieService);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void continuesWithoutAuthenticationWhenTokenIsAbsentOrBlank() throws Exception {
    when(cookieService.extractAccessTokenFromCookie(request)).thenReturn(null, "   ");

    filter.doFilter(request, response, filterChain);
    filter.doFilter(request, response, filterChain);

    verify(filterChain, org.mockito.Mockito.times(2)).doFilter(request, response);
    verify(userDetailsService, never())
        .loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void establishesAuthenticationForValidTokenAndAvoidsReauthentication() throws Exception {
    when(cookieService.extractAccessTokenFromCookie(request)).thenReturn("jwt", "other-jwt");
    when(jwtService.extractUsername("jwt")).thenReturn("alice");
    when(jwtService.isTokenValid(
            org.mockito.ArgumentMatchers.eq("jwt"),
            org.mockito.ArgumentMatchers.any(UserDetails.class)))
        .thenReturn(true);
    final UserDetails user =
        User.withUsername("alice").password("hash").authorities("ROLE_ADMIN").build();
    when(userDetailsService.loadUserByUsername("alice")).thenReturn(user);

    filter.doFilter(request, response, filterChain);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());

    filter.doFilter(request, response, filterChain);
    verify(userDetailsService).loadUserByUsername("alice");
    verify(filterChain, org.mockito.Mockito.times(2)).doFilter(request, response);
  }

  @Test
  void invalidTokenContinuesWithoutSettingAuthentication() throws Exception {
    when(cookieService.extractAccessTokenFromCookie(request)).thenReturn("jwt");
    when(jwtService.extractUsername("jwt")).thenReturn("alice");
    when(userDetailsService.loadUserByUsername("alice"))
        .thenReturn(User.withUsername("alice").password("hash").authorities("ROLE_ADMIN").build());
    when(jwtService.isTokenValid(
            org.mockito.ArgumentMatchers.eq("jwt"),
            org.mockito.ArgumentMatchers.any(UserDetails.class)))
        .thenReturn(false);

    filter.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void disabledOrLockedAccountIsRejected() {
    when(cookieService.extractAccessTokenFromCookie(request)).thenReturn("jwt");
    when(jwtService.extractUsername("jwt")).thenReturn("alice");
    when(userDetailsService.loadUserByUsername("alice"))
        .thenReturn(
            User.withUsername("alice")
                .password("hash")
                .authorities("ROLE_ADMIN")
                .disabled(true)
                .build());

    assertThrows(
        UnauthorizedException.class, () -> filter.doFilter(request, response, filterChain));
  }
}
