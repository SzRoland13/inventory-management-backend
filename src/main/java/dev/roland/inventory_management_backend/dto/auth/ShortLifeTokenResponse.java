package dev.roland.inventory_management_backend.dto.auth;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortLifeTokenResponse {

  private boolean twoFactorEnabled;
  private String shortLifeToken;
  private Instant expiresAt;
}
