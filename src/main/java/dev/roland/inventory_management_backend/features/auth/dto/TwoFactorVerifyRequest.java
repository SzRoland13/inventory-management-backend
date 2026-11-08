package dev.roland.inventory_management_backend.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.EmailAddress;
import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import lombok.Data;

/** Carries the TOTP code submitted to verify two-factor authentication. */
@Data
public class TwoFactorVerifyRequest {

  @NotBlank(message = ValidationKeys.REQUIRED)
  @EmailAddress
  @Size(max = 254, message = ValidationKeys.INVALID_EMAIL)
  private String email;

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Pattern(regexp = "[0-9]{6}", message = ValidationKeys.INVALID_VALUE)
  private String code;

  @NotBlank(message = ValidationKeys.REQUIRED)
  private String shortLifeToken;
}
