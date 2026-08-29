package dev.roland.inventory_management_backend.service.common;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.model.CustomUserDetails;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.service.UserService;
import lombok.RequiredArgsConstructor;

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
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    User user = userService.findByUsernameOrThrow(username);

    return new CustomUserDetails(user);
  }
}
