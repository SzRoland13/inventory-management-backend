package dev.roland.inventory_management_backend.common.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

/** Collects application settings used across the application. */
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

  @Value("${app.storage.public-endpoint:${app.storage.endpoint}}")
  private String s3PublicEndpoint;

  @Value("${app.storage.region}")
  private String s3Region;

  @Value("${app.storage.access-key}")
  private String s3AccessKey;

  @Value("${app.storage.secret-key}")
  private String s3SecretKey;

  @Value("${redis.session.ttl-minutes}")
  private long sessionTtlMinutes;

  @Value("${app.initial-admin.username:}")
  private String initialAdminUsername;

  @Value("${app.initial-admin.email:}")
  private String initialAdminEmail;

  @Value("${app.initial-company.name:}")
  private String initialCompanyName;

  @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
  private String databaseDriverClassName;

  @Value("${app.outbox.encryption-secret:}")
  private String outboxEncryptionSecret;

  @Value("${app.outbox.publish-interval-ms:1000}")
  private long outboxPublishIntervalMs;

  @Value("${app.outbox.publish-batch-size:50}")
  private int outboxPublishBatchSize;

  @Value("${app.outbox.publish-confirm-timeout-ms:10000}")
  private int outboxPublishConfirmTimeoutMs;

  @Value("${app.outbox.publish-lease-seconds:120}")
  private int outboxPublishLeaseSeconds;

  @Value("${app.outbox.max-attempts:5}")
  private int outboxMaxAttempts;

  @Value("${app.outbox.retry-delay-ms:3000}")
  private int outboxRetryDelayMs;

  @Value("${app.document-pdf.renderer-url:http://localhost:8081}")
  private String documentPdfRendererUrl;

  @Value("${app.document-pdf.renderer-path:/api/v1/render}")
  private String documentPdfRendererPath;

  @Value("${app.document-pdf.renderer-token:}")
  private String documentPdfRendererToken;

  @Value("${app.document-pdf.max-attempts:5}")
  private int documentPdfMaxAttempts;

  @Value("${app.mail.from:noreply@example.com}")
  private String mailFrom;

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;
}
