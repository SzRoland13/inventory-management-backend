package dev.roland.inventory_management_backend.features.auth.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Combines authentication tokens with the user details returned after login. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  private UserDetails user;

  @JsonProperty("firstTime2FAEnabled")
  private boolean firstTimeTwoFaEnabled;

  /** Carries the user profile fields returned alongside authentication tokens. */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class UserDetails {
    private Long id;
    private String email;
    private String username;
    private UserRole role;
    private Long avatarId;
    private String avatarUrl;
    private Instant avatarUrlExpiry;
  }
}
