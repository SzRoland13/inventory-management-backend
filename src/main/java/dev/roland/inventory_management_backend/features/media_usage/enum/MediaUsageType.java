package dev.roland.inventory_management_backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaUsageType {
  /** Represents the logo value. */
  LOGO("logo"),
  /** Represents the avatar value. */
  AVATAR("avatar"),
  /** Represents the image value. */
  IMAGE("image"),
  /** Represents the other value. */
  OTHER("other"),
  ;

  private final String name;
}
