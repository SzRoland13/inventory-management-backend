package dev.roland.inventory_management_backend.common.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Defines the cross-origin policy for credentialed frontend requests. */
@Configuration
public class CorsConfiguration {

  /**
   * Defines the origins and request options allowed by CORS.
   *
   * @return CORS policy source used by Spring MVC and Security
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final org.springframework.web.cors.CorsConfiguration configuration =
        new org.springframework.web.cors.CorsConfiguration();

    configuration.setAllowedOrigins(List.of("http://localhost:3001"));
    configuration.setAllowCredentials(true);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        List.of("X-Requested-With", "Content-Type", "Cookie", "X-XSRF-TOKEN", "Authorization"));

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
