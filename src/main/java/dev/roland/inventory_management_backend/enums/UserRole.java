package dev.roland.inventory_management_backend.enums;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserRole {
  /** Represents the admin value. */
  ADMIN,
  /** Represents the manager value. */
  MANAGER,
  /** Represents the sales value. */
  SALES,
  ;

  /** Returns this value in Spring Security authority format. */
  public String getAsAuthority() {
    return "ROLE_" + this.name();
  }

  /** Converts this value to a Spring Security authority. */
  public SimpleGrantedAuthority toGrantedAuthority() {
    return new SimpleGrantedAuthority(getAsAuthority());
  }

  /** Resolves a role name without regard to case. */
  public static Optional<UserRole> fromString(String value) {
    return Arrays.stream(values()).filter(r -> r.name().equalsIgnoreCase(value)).findFirst();
  }
}
