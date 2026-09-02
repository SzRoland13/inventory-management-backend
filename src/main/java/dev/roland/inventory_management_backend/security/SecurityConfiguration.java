package dev.roland.inventory_management_backend.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import dev.roland.inventory_management_backend.common.service.CustomUserDetailsService;
import dev.roland.inventory_management_backend.features.auth.AuthController;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final CustomUserDetailsService userDetailsService;
  private final AuthenticationFilter authenticationFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    (request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public auth endpoints
                    .requestMatchers(
                        AuthController.AUTH_BASE_ENDPOINT
                            + AuthController.CHECK_FIRST_LOGIN_ENDPOINT,
                        AuthController.AUTH_BASE_ENDPOINT + AuthController.SEND_OTC_ENDPOINT,
                        AuthController.AUTH_BASE_ENDPOINT + AuthController.VALIDATE_OTC_ENDPOINT,
                        AuthController.AUTH_BASE_ENDPOINT + AuthController.SETUP_PASSWORD_ENDPOINT,
                        AuthController.AUTH_BASE_ENDPOINT + AuthController.LOGIN_ENDPOINT,
                        AuthController.AUTH_BASE_ENDPOINT + AuthController.TWO_FA_SETUP_ENDPOINT,
                        AuthController.AUTH_BASE_ENDPOINT + AuthController.TWO_FA_LOGIN_ENDPOINT,
                        AuthController.AUTH_BASE_ENDPOINT + AuthController.REFRESH_ENDPOINT,
                        AuthController.AUTH_BASE_ENDPOINT + AuthController.LOGOUT_ENDPOINT)
                    .permitAll()

                    // Setup-required users allowed
                    .requestMatchers(
                        AuthController.AUTH_BASE_ENDPOINT + AuthController.CHECK_SESSION_ENDPOINT)
                    .hasAnyAuthority(
                        UserStatus.asAuthorities(UserStatus.ACTIVE, UserStatus.SETUP_REQUIRED))

                    // Everything else → only ACTIVE
                    .anyRequest()
                    .hasAuthority(UserStatus.ACTIVE.getAsAuthority()))
        .userDetailsService(userDetailsService);

    http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
