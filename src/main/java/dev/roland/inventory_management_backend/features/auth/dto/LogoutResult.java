package dev.roland.inventory_management_backend.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogoutResult {

  private boolean clearAccessToken;
  private boolean clearRefreshToken;
}
