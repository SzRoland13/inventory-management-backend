package dev.roland.inventory_management_backend.enums;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserStatus {
  ACTIVE,
  SUSPENDED,
  SETUP_REQUIRED,
  ;

  public String getAsAuthority() {
    return "STATUS_" + this.name();
  }

  public SimpleGrantedAuthority toGrantedAuthority() {
    return new SimpleGrantedAuthority(getAsAuthority());
  }

  public static String[] asAuthorities(UserStatus... statuses) {
    return Arrays.stream(statuses).map(UserStatus::getAsAuthority).toArray(String[]::new);
  }

  public static Optional<UserStatus> fromString(String value) {
    return Arrays.stream(values())
        .filter(status -> status.name().equalsIgnoreCase(value))
        .findFirst();
  }
}
