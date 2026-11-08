package dev.roland.inventory_management_backend.features.auth.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Carries a short-lived token that authorizes first-login password setup. */
@Data
@AllArgsConstructor
public class PasswordSetupTokenResponse {
  private String setupToken;
  private Instant expiresAt;
}
