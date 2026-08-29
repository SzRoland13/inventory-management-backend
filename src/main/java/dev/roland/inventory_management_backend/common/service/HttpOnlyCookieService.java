package dev.roland.inventory_management_backend.common.service;

import java.util.Arrays;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

@Service
public class HttpOnlyCookieService {
  public static String ACCESS_TOKEN = "access_token";
  public static String REFRESH_TOKEN = "refresh_token";

  /**
   * Extracts the access token from the configured HTTP-only cookie.
   *
   * @param request incoming servlet request
   * @return access token value, or null when the cookie is absent
   */
  public String extractAccessTokenFromCookie(HttpServletRequest request) {
    return extractToken(request, ACCESS_TOKEN);
  }

  /**
   * Extracts the refresh token from the configured HTTP-only cookie.
   *
   * @param request incoming servlet request
   * @return refresh token value, or null when the cookie is absent
   */
  public String extractRefreshTokenFromCookie(HttpServletRequest request) {
    return extractToken(request, REFRESH_TOKEN);
  }

  private String extractToken(HttpServletRequest request, String tokenName) {
    if (request.getCookies() == null) return null;

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
      HttpServletResponse response, String token, boolean secure, int maxAge) {
    response.addCookie(createCookie(ACCESS_TOKEN, token, secure, maxAge));
  }

  /**
   * Clears the access token cookie by setting it to an empty expired cookie.
   *
   * @param response servlet response to mutate
   * @param secure whether the clearing cookie should require HTTPS
   */
  public void clearAccessCookie(HttpServletResponse response, boolean secure) {
    response.addCookie(createCookie(ACCESS_TOKEN, "", secure, 0));
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
      HttpServletResponse response, String token, boolean secure, int maxAge) {
    response.addCookie(createCookie(REFRESH_TOKEN, token, secure, maxAge));
  }

  /**
   * Clears the refresh token cookie by setting it to an empty expired cookie.
   *
   * @param response servlet response to mutate
   * @param secure whether the clearing cookie should require HTTPS
   */
  public void clearRefreshCookie(HttpServletResponse response, boolean secure) {
    response.addCookie(createCookie(REFRESH_TOKEN, "", secure, 0));
  }

  private Cookie createCookie(String name, String token, boolean secure, int maxAge) {
    Cookie cookie = new Cookie(name, token);
    cookie.setHttpOnly(true);
    cookie.setSecure(secure);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);

    return cookie;
  }
}
