package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;

@ExtendWith(MockitoExtension.class)
class LoginSessionServiceTest {

  @Mock private RedisTemplate<String, String> redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;
  @Mock private AppConfiguration configuration;

  private LoginSessionService service;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    service = new LoginSessionService(redisTemplate, configuration);
  }

  @Test
  void storesTemporarySessionWithConfiguredTtlAndReturnsExpiry() {
    when(configuration.getSessionTtlMinutes()).thenReturn(8L);

    final var result = service.createTemporarySessionWithExpiry("alice@example.com");

    assertTrue(result.getToken().matches("[0-9a-f-]{36}"));
    assertTrue(result.getExpiresAt().isAfter(java.time.Instant.now()));
    verify(valueOperations).set(result.getToken(), "alice@example.com", 8L, TimeUnit.MINUTES);
  }

  @Test
  void readsAndDeletesSessionsAndTreatsOnlyTrueRedisKeysAsValid() {
    when(valueOperations.get("session")).thenReturn("alice@example.com");
    when(redisTemplate.hasKey("present")).thenReturn(true);
    when(redisTemplate.hasKey("missing")).thenReturn(false);
    when(redisTemplate.hasKey("unknown")).thenReturn(null);

    assertEquals("alice@example.com", service.consumeSessionToken("session"));
    assertTrue(service.isValid("present"));
    assertFalse(service.isValid("missing"));
    assertFalse(service.isValid("unknown"));
    service.deleteToken("session");
    verify(redisTemplate).delete("session");
  }
}
