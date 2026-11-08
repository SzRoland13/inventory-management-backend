package dev.roland.inventory_management_backend.features.auth.dto;

import jakarta.validation.constraints.NotBlank;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import lombok.Data;

/** Carries the refresh token used to request a new access token. */
@Data
public class RefreshRequest {

  @NotBlank(message = ValidationKeys.REQUIRED)
  private String refreshToken;
}
