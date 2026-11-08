package dev.roland.inventory_management_backend.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.EmailAddress;
import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import lombok.Data;

/** Carries the credentials submitted to authenticate an account. */
@Data
public class LoginRequest {

  @NotBlank(message = ValidationKeys.REQUIRED)
  @EmailAddress
  @Size(max = 254, message = ValidationKeys.INVALID_EMAIL)
  private String email;

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Size(max = 128, message = ValidationKeys.TOO_LONG)
  private String password;
}
