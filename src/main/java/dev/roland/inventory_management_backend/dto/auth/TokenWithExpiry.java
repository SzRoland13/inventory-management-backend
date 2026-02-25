package dev.roland.inventory_management_backend.dto.auth;

import java.time.Instant;

import lombok.Data;

@Data
public class TokenWithExpiry {
  private final String token;
  private final Instant expiresAt;
}
