package dev.roland.inventory_management_backend.features.auth.dto;

import jakarta.validation.constraints.NotBlank;

import dev.roland.inventory_management_backend.common.annotation.PasswordsMatch;
import dev.roland.inventory_management_backend.common.annotation.ValidPassword;
import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import lombok.Data;

/** Carries the new password and confirmation submitted during account setup. */
@Data
@PasswordsMatch
public class PasswordSetupRequest {

  @NotBlank(message = ValidationKeys.REQUIRED)
  private String oneTimeCode;

  @ValidPassword private String password;

  @NotBlank(message = ValidationKeys.REQUIRED)
  private String repeatPassword;
}
