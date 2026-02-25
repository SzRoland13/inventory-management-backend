package dev.roland.inventory_management_backend.service.common;

import dev.roland.inventory_management_backend.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.dto.auth.TokenWithExpiry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginSessionService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AppConfiguration appConfiguration;

    /**
     * Generate and store a short-life login session token.
     *
     * @param email user email
     * @return generated token
     */
    public TokenWithExpiry createTemporarySessionWithExpiry(String email) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(appConfiguration.getSessionTtlMinutes() * 60);
        redisTemplate.opsForValue().set(token, email, appConfiguration.getSessionTtlMinutes(), TimeUnit.MINUTES);
        return new TokenWithExpiry(token, expiresAt);
    }

    /**
     * Validate and consume a temporary session token.
     * @param token the short-life token
     * @return email associated with the token
     */
    public String consumeSessionToken(String token) {
        String email = redisTemplate.opsForValue().get(token);
        if (email != null) {
            redisTemplate.delete(token);
        }
        return email;
    }

    /**
     * Check if token exists and is still valid.
     */
    public boolean isValid(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}
