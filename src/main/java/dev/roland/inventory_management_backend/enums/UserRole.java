package dev.roland.inventory_management_backend.enums;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserRole {
  ADMIN,
  MANAGER,
  SALES,
  ;

  public String getAsAuthority() {
    return "ROLE_" + this.name();
  }

  public SimpleGrantedAuthority toGrantedAuthority() {
    return new SimpleGrantedAuthority(getAsAuthority());
  }

  public static Optional<UserRole> fromString(String value) {
    return Arrays.stream(values()).filter(r -> r.name().equalsIgnoreCase(value)).findFirst();
  }
}
