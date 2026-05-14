package dev.roland.inventory_management_backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaUsageType {
  LOGO("logo"),
  AVATAR("avatar"),
  IMAGE("image"),
  OTHER("other"),
  ;

  private final String name;
}
