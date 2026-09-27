package dev.roland.inventory_management_backend.common.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

/** Collects application settings for authentication, storage, mail, and Redis services. */
@Getter
@Component
public class AppConfiguration {

  @Value("${app.cors.allowed-origins}")
  private List<String> corsAllowedOrigins;

  @Value("${app.security.secure-cookie}")
  private boolean secureCookie;

  @Value("${app.security.jwt.access-cookie-name}")
  private String accessTokenCookieName;

  @Value("${app.security.jwt.refresh-cookie-name}")
  private String refreshTokenCookieName;

  @Value("${app.security.jwt.access-expiration-time}")
  private long accessTokenExpirationTime;

  @Value("${app.security.jwt.refresh-expiration-time}")
  private long refreshTokenExpirationTime;

  @Value("${app.security.jwt.secret}")
  private String secret;

  @Value("${app.storage.bucket}")
  private String s3Bucket;

  @Value("${app.storage.endpoint}")
  private String s3Endpoint;

  @Value("${app.storage.region}")
  private String s3Region;

  @Value("${app.storage.access-key}")
  private String s3AccessKey;

  @Value("${app.storage.secret-key}")
  private String s3SecretKey;

  @Value("${redis.session.ttl-minutes}")
  private long sessionTtlMinutes;
}
