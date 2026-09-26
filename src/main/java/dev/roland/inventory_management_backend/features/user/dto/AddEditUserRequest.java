package dev.roland.inventory_management_backend.features.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/** Carries the profile and role fields used to create or update a user. */
@Data
public class AddEditUserRequest {

  @NotBlank(message = "Username cannot be empty")
  private String username;

  @NotBlank(message = "Email cannot be empty")
  @Email
  private String email;

  @NotBlank(message = "Role cannot be empty")
  private String role;
}
