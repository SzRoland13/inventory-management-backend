package dev.roland.inventory_management_backend.service.common;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.dto.auth.TokenWithExpiry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginSessionService {

  private final RedisTemplate<String, String> redisTemplate;
  private final AppConfiguration appConfiguration;

  /**
   * Generate and store a short-life login session token.
   *
   * @param email user email
   * @return generated token with its expiry timestamp
   */
  public TokenWithExpiry createTemporarySessionWithExpiry(String email) {
    String token = UUID.randomUUID().toString();
    Instant expiresAt = Instant.now().plusSeconds(appConfiguration.getSessionTtlMinutes() * 60);
    redisTemplate
        .opsForValue()
        .set(token, email, appConfiguration.getSessionTtlMinutes(), TimeUnit.MINUTES);
    return new TokenWithExpiry(token, expiresAt);
  }

  /**
   * Validate and consume a temporary session token.
   *
   * @param token the short-life token
   * @return email associated with the token, or null when the token is missing or expired
   */
  public String consumeSessionToken(String token) {
    return redisTemplate.opsForValue().get(token);
  }

  /**
   * Deletes a temporary login session token from Redis.
   *
   * @param token token to delete
   */
  public void deleteToken(String token) {
    redisTemplate.delete(token);
  }

  /**
   * Checks whether a temporary login session token exists and is still valid.
   *
   * @param token token to validate
   * @return true when Redis still contains the token
   */
  public boolean isValid(String token) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(token));
  }
}
