package dev.roland.inventory_management_backend.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginFinalizationResult {
  private LoginResponse loginResponse;
  private AuthTokens tokens;
  private boolean firstTime2faEnabled;
}
