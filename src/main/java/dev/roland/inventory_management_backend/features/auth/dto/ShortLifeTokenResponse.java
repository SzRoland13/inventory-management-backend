package dev.roland.inventory_management_backend.features.auth.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

/** Returns a short-lived token used to continue an in-progress login. */
@Data
@Builder
public class ShortLifeTokenResponse {

  private boolean twoFactorEnabled;
  private String shortLifeToken;
  private Instant expiresAt;
}
