package dev.roland.inventory_management_backend.features.user.service.impl;

import java.util.Objects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.enums.UserRole;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.message.UserMessageKey;
import dev.roland.inventory_management_backend.features.user.repository.UserRepository;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<User, Long> getRepository() {
    return userRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.USER;
  }

  /**
   * Retrieves a {@link User} entity from the database that matches the given email address.
   *
   * @param email the email address to look up
   * @return the found {@link User}, or throws a {@link NotFoundException} if not found
   */
  @Override
  public User findUserByEmailOrThrow(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.USER));
  }

  /**
   * Retrieves a {@link User} entity from the database that matches the given username.
   *
   * @param username the username to look up
   * @return the found {@link User}, or throws a {@link NotFoundException} if not found
   */
  @Override
  public User findByUsernameOrThrow(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.USER));
  }

  /**
   * Updates the user with that data passed.
   *
   * @param id - the id of the user to update
   * @param request - the data to update the user
   * @return the updated user in a {@link UserDto}
   */
  @Override
  public UserDto updateUser(Long id, AddEditUserRequest request) {
    User updated =
        update(
            id,
            user -> {
              if (!Objects.equals(user.getEmail(), request.getEmail())) {
                user.setOtcSetupComplete(false);
                user.setTotpSecret(null);
                user.set2faEnabled(false);
                user.setPassword(null);
              }

              user.setEmail(request.getEmail());
              user.setUsername(request.getUsername());

              try {
                user.setRole(UserRole.valueOf(request.getRole()));
              } catch (IllegalArgumentException e) {
                throw new ApiException(UserMessageKey.INVALID_ROLE);
              }
            });

    return new UserDto(updated);
  }
}
