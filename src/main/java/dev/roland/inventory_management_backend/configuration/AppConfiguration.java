package dev.roland.inventory_management_backend.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppConfiguration {

    @Value("${app.security.secure-cookie}")
    private boolean secureCookie;

    @Value("${security.jwt.access-expiration-time}")
    private long accessTokenExpirationTime;

    @Value("${security.jwt.refresh-expiration-time}")
    private long refreshTokenExpirationTime;

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${redis.session.ttl-minutes}")
    private long sessionTtlMinutes;
}
