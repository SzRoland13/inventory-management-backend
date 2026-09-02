package dev.roland.inventory_management_backend.features.auth.dto;

import java.time.Instant;

import dev.roland.inventory_management_backend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  private UserDetails user;
  private boolean firstTime2FAEnabled;

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
