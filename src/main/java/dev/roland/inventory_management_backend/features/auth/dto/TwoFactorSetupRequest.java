package dev.roland.inventory_management_backend.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/** Carries the identity and short-lived session token for two-factor setup. */
@Data
public class TwoFactorSetupRequest {

  @NotBlank(message = "Email cannot be empty")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Short life token cannot be empty")
  private String shortLifeToken;
}
