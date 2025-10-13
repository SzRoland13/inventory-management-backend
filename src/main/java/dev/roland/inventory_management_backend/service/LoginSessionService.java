package dev.roland.inventory_management_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginSessionService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final long SESSION_TTL_MINUTES = 5;

    /**
     * Generate and store a short-life login session token.
     *
     * @param email user email
     * @return generated token
     */
    public String createTemporarySession(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(token, email, SESSION_TTL_MINUTES, TimeUnit.MINUTES);

        return token;
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
