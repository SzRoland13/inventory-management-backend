package dev.roland.inventory_management_backend.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dev.roland.inventory_management_backend.enums.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Adapts a persisted {@link User} to Spring Security's {@code UserDetails} contract.
 *
 * <p>This is not a database entity. It exposes the user's username, password, role, and account
 * status to authentication and authorization infrastructure. A suspended user is considered both
 * disabled and locked.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

  private final User user;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(user.getRole().toGrantedAuthority(), user.getStatus().toGrantedAuthority());
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonLocked() {
    return user.getStatus() != UserStatus.SUSPENDED;
  }

  @Override
  public boolean isEnabled() {
    return user.getStatus() != UserStatus.SUSPENDED;
  }
}
