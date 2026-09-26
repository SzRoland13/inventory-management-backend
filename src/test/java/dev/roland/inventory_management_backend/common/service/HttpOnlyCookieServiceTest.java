package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    final Cookie[] cookies = response.getCookies();
    assertEquals(2, cookies.length);
    assertEquals("access", cookies[0].getName());
    assertEquals("jwt", cookies[0].getValue());
    assertTrue(cookies[0].isHttpOnly());
    assertTrue(cookies[0].getSecure());
    assertEquals("/", cookies[0].getPath());
    assertEquals(30, cookies[0].getMaxAge());
    assertEquals("refresh", cookies[1].getName());
    assertEquals("refresh-jwt", cookies[1].getValue());
    assertTrue(cookies[1].isHttpOnly());
    assertEquals(60, cookies[1].getMaxAge());
  }

  @Test
  void clearsBothCookiesByExpiringThem() {
    final MockHttpServletResponse response = new MockHttpServletResponse();

    service.clearAccessCookie(response, true);
    service.clearRefreshCookie(response, true);

    assertEquals(0, response.getCookies()[0].getMaxAge());
    assertEquals("", response.getCookies()[0].getValue());
    assertEquals(0, response.getCookies()[1].getMaxAge());
    assertEquals("", response.getCookies()[1].getValue());
  }
}
