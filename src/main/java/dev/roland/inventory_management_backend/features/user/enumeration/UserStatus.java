package dev.roland.inventory_management_backend.features.user.enumeration;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Defines account states used to control whether a user may authenticate. */
public enum UserStatus {
  /** Represents the active value. */
  ACTIVE,
  /** Represents the suspended value. */
  SUSPENDED,
  /** Represents the setup required value. */
  SETUP_REQUIRED,
  ;

  /**
   * Returns this value in Spring Security authority format.
   *
   * @return status authority name
   */
  public String getAsAuthority() {
    return "STATUS_" + this.name();
  }

  /**
   * Converts this value to a Spring Security authority.
   *
   * @return granted authority for this status
   */
  public SimpleGrantedAuthority toGrantedAuthority() {
    return new SimpleGrantedAuthority(getAsAuthority());
  }

  /**
   * Converts statuses to Spring Security authority names.
   *
   * @param statuses statuses to convert
   * @return corresponding authority names
   */
  public static String[] asAuthorities(final UserStatus... statuses) {
    return Arrays.stream(statuses).map(UserStatus::getAsAuthority).toArray(String[]::new);
  }

  /**
   * Resolves a status name without regard to case.
   *
   * @param value status name to resolve
   * @return matching status, or empty when the name is unknown
   */
  public static Optional<UserStatus> fromString(final String value) {
    return Arrays.stream(values())
        .filter(status -> status.name().equalsIgnoreCase(value))
        .findFirst();
  }
}
