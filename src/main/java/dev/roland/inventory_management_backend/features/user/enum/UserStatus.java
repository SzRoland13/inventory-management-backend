package dev.roland.inventory_management_backend.enums;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserStatus {
  /** Represents the active value. */
  ACTIVE,
  /** Represents the suspended value. */
  SUSPENDED,
  /** Represents the setup required value. */
  SETUP_REQUIRED,
  ;

  /** Returns this value in Spring Security authority format. */
  public String getAsAuthority() {
    return "STATUS_" + this.name();
  }

  /** Converts this value to a Spring Security authority. */
  public SimpleGrantedAuthority toGrantedAuthority() {
    return new SimpleGrantedAuthority(getAsAuthority());
  }

  /** Converts statuses to Spring Security authority names. */
  public static String[] asAuthorities(UserStatus... statuses) {
    return Arrays.stream(statuses).map(UserStatus::getAsAuthority).toArray(String[]::new);
  }

  /** Resolves a status name without regard to case. */
  public static Optional<UserStatus> fromString(String value) {
    return Arrays.stream(values())
        .filter(status -> status.name().equalsIgnoreCase(value))
        .findFirst();
  }
}
