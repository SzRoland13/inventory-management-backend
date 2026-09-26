package dev.roland.inventory_management_backend.features.user.dto;

import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Exposes the user identity, role, and account state returned by user endpoints. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

  /**
   * Creates a user response from the supplied account fields.
   *
   * @param user account fields to expose
   */
  public UserDto(final User user) {
    this.id = user.getId();
    this.username = user.getUsername();
    this.email = user.getEmail();
    this.role = user.getRole();
    this.twoFaEnabled = user.is2faEnabled();
    this.otcSetupCompleted = user.isOtcSetupComplete();
    this.userStatus = user.getStatus();
  }

  private long id;
  private String username;
  private String email;
  private UserRole role;
  private boolean twoFaEnabled;
  private boolean otcSetupCompleted;
  private UserStatus userStatus;
}
