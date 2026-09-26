package dev.roland.inventory_management_backend.features.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Serializable authentication principal containing a snapshot of a user's identity and status.
 *
 * <p>This is not a database entity and intentionally keeps only scalar serializable fields. A
 * suspended user is considered both disabled and locked.
 *
 * @return get authorities result
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

  private final Long userId;
  private final String username;
  private final String password;
  private final UserRole role;
  private final UserStatus status;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(role.toGrantedAuthority(), status.toGrantedAuthority());
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonLocked() {
    return status != UserStatus.SUSPENDED;
  }

  @Override
  public boolean isEnabled() {
    return status != UserStatus.SUSPENDED;
  }
}
