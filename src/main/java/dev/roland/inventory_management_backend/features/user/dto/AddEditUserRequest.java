package dev.roland.inventory_management_backend.features.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.EmailAddress;
import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import lombok.Data;

/** Carries the profile and role fields used to create or update a user. */
@Data
public class AddEditUserRequest {

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Size(max = 100, message = ValidationKeys.TOO_LONG)
  private String username;

  @NotBlank(message = ValidationKeys.REQUIRED)
  @EmailAddress
  @Size(max = 254, message = ValidationKeys.INVALID_EMAIL)
  private String email;

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Pattern(regexp = "ADMIN|MANAGER|SALES", message = ValidationKeys.INVALID_VALUE)
  private String role;
}
