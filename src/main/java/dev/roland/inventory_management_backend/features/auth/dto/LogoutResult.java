package dev.roland.inventory_management_backend.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Describes whether logout cleared the user’s authentication tokens. */
@Data
@AllArgsConstructor
public class LogoutResult {

  private boolean clearAccessToken;
  private boolean clearRefreshToken;
}
