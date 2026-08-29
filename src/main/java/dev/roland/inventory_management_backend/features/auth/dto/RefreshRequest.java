package dev.roland.inventory_management_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class RefreshRequest {

  @NotBlank(message = "Refresh token cannot be empty")
  private String refreshToken;
}
