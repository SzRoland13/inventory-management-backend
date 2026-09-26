package dev.roland.inventory_management_backend.common.service;

import java.util.Arrays;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

/** Creates, clears, and reads the HTTP-only access and refresh cookies used by authentication. */
@Service
@RequiredArgsConstructor
public class HttpOnlyCookieService {

  private final AppConfiguration appConfiguration;

  /**
   * Extracts the access token from the configured HTTP-only cookie.
   *
   * @param request incoming servlet request
   * @return access token value, or null when the cookie is absent
   */
  public String extractAccessTokenFromCookie(final HttpServletRequest request) {
    return extractToken(request, appConfiguration.getAccessTokenCookieName());
  }

  /**
   * Extracts the refresh token from the configured HTTP-only cookie.
   *
   * @param request incoming servlet request
   * @return refresh token value, or null when the cookie is absent
   */
  public String extractRefreshTokenFromCookie(final HttpServletRequest request) {
    return extractToken(request, appConfiguration.getRefreshTokenCookieName());
  }

  private String extractToken(final HttpServletRequest request, final String tokenName) {
    if (request.getCookies() == null) {
      return null;
    }

    return Arrays.stream(request.getCookies())
        .filter(cookie -> tokenName.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }

  /**
   * Adds an HTTP-only access token cookie to the response.
   *
   * @param response servlet response to mutate
   * @param token access token value
   * @param secure whether the cookie should require HTTPS
   * @param maxAge cookie max age in seconds
   */
  public void setAccessCookie(
      final HttpServletResponse response,
      final String token,
      final boolean secure,
      final int maxAge) {
    response.addCookie(
        createCookie(appConfiguration.getAccessTokenCookieName(), token, secure, maxAge));
  }

  /**
   * Clears the access token cookie by setting it to an empty expired cookie.
   *
   * @param response servlet response to mutate
   * @param secure whether the clearing cookie should require HTTPS
   */
  public void clearAccessCookie(final HttpServletResponse response, final boolean secure) {
    response.addCookie(createCookie(appConfiguration.getAccessTokenCookieName(), "", secure, 0));
  }

  /**
   * Adds an HTTP-only refresh token cookie to the response.
   *
   * @param response servlet response to mutate
   * @param token refresh token value
   * @param secure whether the cookie should require HTTPS
   * @param maxAge cookie max age in seconds
   */
  public void setRefreshCookie(
      final HttpServletResponse response,
      final String token,
      final boolean secure,
      final int maxAge) {
    response.addCookie(
        createCookie(appConfiguration.getRefreshTokenCookieName(), token, secure, maxAge));
  }

  /**
   * Clears the refresh token cookie by setting it to an empty expired cookie.
   *
   * @param response servlet response to mutate
   * @param secure whether the clearing cookie should require HTTPS
   */
  public void clearRefreshCookie(final HttpServletResponse response, final boolean secure) {
    response.addCookie(createCookie(appConfiguration.getRefreshTokenCookieName(), "", secure, 0));
  }

  @SuppressFBWarnings(
      value = "INSECURE_COOKIE",
      justification =
          "Secure cookies are configurable for local HTTP; deployments must enable them.")
  private Cookie createCookie(
      final String name, final String token, final boolean secure, final int maxAge) {
    final Cookie cookie = new Cookie(name, token);
    cookie.setHttpOnly(true);
    cookie.setSecure(secure);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);

    return cookie;
  }
}
