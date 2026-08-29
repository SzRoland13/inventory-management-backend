package dev.roland.inventory_management_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class TwoFactorVerifyRequest {

  @NotBlank(message = "Email cannot be empty")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Verifier code cannot be empty")
  private String code;

  @NotBlank(message = "Short life token cannot be empty")
  private String shortLifeToken;
}
