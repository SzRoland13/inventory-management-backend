package dev.roland.inventory_management_backend.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckFirstLoginResponse {
  private boolean emailRegistered;
  private boolean firstLogin;
}
