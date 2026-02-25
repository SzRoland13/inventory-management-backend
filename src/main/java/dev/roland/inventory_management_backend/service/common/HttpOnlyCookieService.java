package dev.roland.inventory_management_backend.service.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;

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
}

