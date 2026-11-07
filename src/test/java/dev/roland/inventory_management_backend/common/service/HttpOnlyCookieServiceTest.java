package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;

@ExtendWith(MockitoExtension.class)
class HttpOnlyCookieServiceTest {

  @Mock private AppConfiguration configuration;

  private HttpOnlyCookieService service;

  @BeforeEach
  void setUp() {
    when(configuration.getAccessTokenCookieName()).thenReturn("access");
    when(configuration.getRefreshTokenCookieName()).thenReturn("refresh");
    service = new HttpOnlyCookieService(configuration);
  }

  @Test
  void extractsConfiguredTokensAndReturnsNullWhenCookieIsMissing() {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("other", "ignored"), new Cookie("access", "access-value"));

    assertEquals("access-value", service.extractAccessTokenFromCookie(request));
    assertNull(service.extractRefreshTokenFromCookie(request));
    assertNull(service.extractAccessTokenFromCookie(new MockHttpServletRequest()));
  }

  @Test
  void setsHttpOnlySecureCookiesWithExpectedPathAndMaxAge() {
    final MockHttpServletResponse response = new MockHttpServletResponse();

    service.setAccessCookie(response, "jwt", true, 30);
    service.setRefreshCookie(response, "refresh-jwt", false, 60);

    final var cookies = response.getHeaders("Set-Cookie");
    assertEquals(2, cookies.size());
    assertTrue(cookies.get(0).contains("access=jwt"));
    assertTrue(cookies.get(0).contains("HttpOnly"));
    assertTrue(cookies.get(0).contains("Secure"));
    assertTrue(cookies.get(0).contains("Path=/"));
    assertTrue(cookies.get(0).contains("Max-Age=30"));
    assertTrue(cookies.get(1).contains("refresh=refresh-jwt"));
    assertTrue(cookies.get(1).contains("HttpOnly"));
    assertFalse(cookies.get(1).contains("; Secure"));
    assertTrue(cookies.get(1).contains("Max-Age=60"));
  }

  @Test
  void clearsBothCookiesByExpiringThem() {
    final MockHttpServletResponse response = new MockHttpServletResponse();

    service.clearAccessCookie(response, true);
    service.clearRefreshCookie(response, true);

    final var cookies = response.getHeaders("Set-Cookie");
    assertTrue(cookies.get(0).contains("access="));
    assertTrue(cookies.get(0).contains("Max-Age=0"));
    assertTrue(cookies.get(1).contains("refresh="));
    assertTrue(cookies.get(1).contains("Max-Age=0"));
  }
}
