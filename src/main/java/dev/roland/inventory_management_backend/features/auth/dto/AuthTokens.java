package dev.roland.inventory_management_backend.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Carries the access and refresh tokens issued by the authentication flow. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokens {
  private String accessToken;
  private String refreshToken;
}
