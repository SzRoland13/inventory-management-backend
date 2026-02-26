package dev.roland.inventory_management_backend.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfiguration {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    org.springframework.web.cors.CorsConfiguration configuration =
        new org.springframework.web.cors.CorsConfiguration();

    configuration.setAllowedOrigins(List.of("http://localhost:3000"));
    configuration.setAllowCredentials(true);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        List.of("X-Requested-With", "Content-Type", "Cookie", "X-XSRF-TOKEN", "Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
