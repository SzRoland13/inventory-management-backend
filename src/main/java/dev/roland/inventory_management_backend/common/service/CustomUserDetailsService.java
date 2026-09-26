package dev.roland.inventory_management_backend.common.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.auth.CustomUserDetails;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

/** Loads a user account and adapts its authentication fields for Spring Security. */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserService userService;

  /**
   * Loads Spring Security user details by username.
   *
   * @param username username to look up
   * @return Spring Security user details for the matching user
   * @throws UsernameNotFoundException when the user cannot be resolved
   */
  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {

    final User user = userService.findByUsernameOrThrow(username);

    return new CustomUserDetails(
        user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.getStatus());
  }
}
