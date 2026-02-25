package dev.roland.inventory_management_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import dev.roland.inventory_management_backend.annotation.PasswordsMatch;
import dev.roland.inventory_management_backend.annotation.ValidPassword;
import lombok.Data;

@Data
@PasswordsMatch
public class PasswordSetupRequest {

  @NotBlank(message = "Email cannot be empty")
  @Email(message = "Invalid email format")
  private String email;

  @ValidPassword private String password;

  @NotBlank(message = "Please repeat your password")
  private String repeatPassword;
}
