package dev.roland.inventory_management_backend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class AppConfiguration {

  @Value("${app.security.secure-cookie}")
  private boolean secureCookie;

  @Value("${app.security.jwt.access-expiration-time}")
  private long accessTokenExpirationTime;

  @Value("${app.security.jwt.refresh-expiration-time}")
  private long refreshTokenExpirationTime;

  @Value("${app.security.jwt.secret}")
  private String secret;

  @Value("${app.storage.bucket}")
  private String S3bucket;

  @Value("${app.storage.endpoint}")
  private String S3Endpoint;

  @Value("${app.storage.region}")
  private String S3Region;

  @Value("${app.storage.access-key}")
  private String S3AccessKey;

  @Value("${app.storage.secret-key}")
  private String S3SecretKey;

  @Value("${redis.session.ttl-minutes}")
  private long sessionTtlMinutes;
}
