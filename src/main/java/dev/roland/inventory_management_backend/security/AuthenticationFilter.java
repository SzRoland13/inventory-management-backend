package dev.roland.inventory_management_backend.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.auth0.jwt.exceptions.JWTVerificationException;

import dev.roland.inventory_management_backend.service.common.CustomUserDetailsService;
import dev.roland.inventory_management_backend.service.common.HttpOnlyCookieService;
import dev.roland.inventory_management_backend.service.common.JwtService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

  private final HandlerExceptionResolver handlerExceptionResolver;
  private final CustomUserDetailsService userDetailsService;
  private final JwtService jwtService;
  private final HttpOnlyCookieService cookieService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String jwt = cookieService.extractAccessTokenFromCookie(request);

    if (jwt != null && !jwt.isBlank()) {
      try {
        String username = jwtService.extractUsername(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

          if (SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(authToken);
          }
        }
      } catch (JWTVerificationException ex) {
        handlerExceptionResolver.resolveException(request, response, null, ex);
      }
    }

    filterChain.doFilter(request, response);
  }
}
