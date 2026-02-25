package dev.roland.inventory_management_backend.service.common;

import java.util.Arrays;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

@Service
public class HttpOnlyCookieService {
  public static String ACCESS_TOKEN = "access_token";
  public static String REFRESH_TOKEN = "refresh_token";

  public String extractAccessTokenFromCookie(HttpServletRequest request) {
    return extractToken(request, ACCESS_TOKEN);
  }

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

  public void setAccessCookie(
      HttpServletResponse response, String token, boolean secure, int maxAge) {
    response.addCookie(createCookie(ACCESS_TOKEN, token, secure, maxAge));
  }

  public void clearAccessCookie(HttpServletResponse response, boolean secure) {
    response.addCookie(createCookie(ACCESS_TOKEN, "", secure, 0));
  }

  public void setRefreshCookie(
      HttpServletResponse response, String token, boolean secure, int maxAge) {
    response.addCookie(createCookie(REFRESH_TOKEN, token, secure, maxAge));
  }

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
