package dev.roland.inventory_management_backend.features.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;

class CustomUserDetailsTest {
  @Test
  void suspendedUsersAreLockedAndDisabledWhileOtherStatusesRemainEnabled() {
    final CustomUserDetails suspended =
        new CustomUserDetails(1L, "suspended", "password", UserRole.ADMIN, UserStatus.SUSPENDED);
    final CustomUserDetails active =
        new CustomUserDetails(2L, "active", "password", UserRole.ADMIN, UserStatus.ACTIVE);

    assertFalse(suspended.isAccountNonLocked());
    assertFalse(suspended.isEnabled());
    assertTrue(active.isAccountNonLocked());
    assertTrue(active.isEnabled());
  }
}
