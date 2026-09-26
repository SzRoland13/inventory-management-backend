package dev.roland.inventory_management_backend.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/** Carries the email address used to begin an authentication flow. */
@Data
public class EmailRequest {

  @NotBlank(message = "Email cannot be empty")
  @Email(message = "Invalid email format")
  private String email;
}
