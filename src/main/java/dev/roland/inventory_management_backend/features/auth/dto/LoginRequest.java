package dev.roland.inventory_management_backend.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/** Carries the credentials submitted to authenticate an account. */
@Data
public class LoginRequest {

  @NotBlank(message = "Email cannot be empty")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password cannot be empty")
  private String password;
}
