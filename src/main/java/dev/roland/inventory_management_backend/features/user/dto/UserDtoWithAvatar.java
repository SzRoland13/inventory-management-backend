package dev.roland.inventory_management_backend.dto.user;

import dev.roland.inventory_management_backend.features.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDtoWithAvatar extends UserDto {
  private String avatarUrl;

  public UserDtoWithAvatar(User user, String avatarUrl) {
    super(user);
    this.avatarUrl = avatarUrl;
  }
}
