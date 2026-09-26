package dev.roland.inventory_management_backend.features.user.enumeration;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Defines the authorization roles assigned to user accounts. */
public enum UserRole {
  /** Represents the admin value. */
  ADMIN,
  /** Represents the manager value. */
  MANAGER,
  /** Represents the sales value. */
  SALES,
  ;

  /**
   * Returns this value in Spring Security authority format.
   *
   * @return role authority name
   */
  public String getAsAuthority() {
    return "ROLE_" + this.name();
  }

  /**
   * Converts this value to a Spring Security authority.
   *
   * @return granted authority for this role
   */
  public SimpleGrantedAuthority toGrantedAuthority() {
    return new SimpleGrantedAuthority(getAsAuthority());
  }

  /**
   * Resolves a role name without regard to case.
   *
   * @param value role name to resolve
   * @return matching role, or empty when the name is unknown
   */
  public static Optional<UserRole> fromString(final String value) {
    return Arrays.stream(values()).filter(r -> r.name().equalsIgnoreCase(value)).findFirst();
  }
}
