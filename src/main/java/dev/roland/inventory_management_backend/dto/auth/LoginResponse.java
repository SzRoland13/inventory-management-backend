package dev.roland.inventory_management_backend.dto.auth;

import dev.roland.inventory_management_backend.enums.UserRole;
import lombok.*;

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
  public static class UserDetails {
    private Long id;
    private String email;
    private String username;
    private UserRole role;
  }
}
