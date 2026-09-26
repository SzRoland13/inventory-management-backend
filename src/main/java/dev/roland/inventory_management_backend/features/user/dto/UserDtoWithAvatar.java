package dev.roland.inventory_management_backend.features.user.dto;

import dev.roland.inventory_management_backend.features.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Adds avatar media details to the user profile response. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDtoWithAvatar extends UserDto {
  private String avatarUrl;

  /**
   * Creates a user response that includes avatar data.
   *
   * @param user account fields to expose
   * @param avatarUrl URL used to display the user's avatar
   */
  public UserDtoWithAvatar(final User user, final String avatarUrl) {
    super(user);
    this.avatarUrl = avatarUrl;
  }
}
