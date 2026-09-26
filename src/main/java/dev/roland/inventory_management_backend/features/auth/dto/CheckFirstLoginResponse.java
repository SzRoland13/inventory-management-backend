package dev.roland.inventory_management_backend.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Reports whether an account still needs to complete first-login setup. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckFirstLoginResponse {
  private boolean emailRegistered;
  private boolean firstLogin;
}
