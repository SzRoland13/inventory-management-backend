package dev.roland.inventory_management_backend.dto.auth;

import lombok.Data;

import java.time.Instant;

@Data
public class TokenWithExpiry {
    private final String token;
    private final Instant expiresAt;
}
