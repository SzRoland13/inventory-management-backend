package dev.roland.inventory_management_backend.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import dev.roland.inventory_management_backend.service.common.HttpOnlyCookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter  extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final MyUserDetailsService userDetailsService;
    private  final JwtUtil jwtUtil;
    private final HttpOnlyCookieService cookieService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String jwt = cookieService.extractAccessTokenFromCookie(request);

        if (jwt != null && !jwt.isBlank()) {
            try {
                String username = jwtUtil.extractUsername(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

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
