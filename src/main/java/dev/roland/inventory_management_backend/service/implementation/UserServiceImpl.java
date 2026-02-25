package dev.roland.inventory_management_backend.service.implementation;

import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.AllUserResponse;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.enums.UserRole;
import dev.roland.inventory_management_backend.exception.ApiException;
import dev.roland.inventory_management_backend.exception.NotFoundException;
import dev.roland.inventory_management_backend.messageKey.AuthMessageKey;
import dev.roland.inventory_management_backend.messageKey.MessageKey;
import dev.roland.inventory_management_backend.messageKey.NotFoundMessageKey;
import dev.roland.inventory_management_backend.messageKey.UserMessageKey;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.repository.UserRepository;
import dev.roland.inventory_management_backend.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public JpaRepository<User, Long> getRepository() {
    return userRepository;
  }

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
   * Validates the current authenticated user session.
   *
   * @param auth the {@link Authentication} object automatically injected by Spring Security,
   *     representing the currently authenticated user
   * @throws ApiException if the authentication is missing, invalid, or the principal cannot be
   *     resolved
   */
  @Override
  public void checkSession(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
      throw new ApiException(AuthMessageKey.INVALID_TOKEN);
    }

    userRepository
        .findByUsername(auth.getName())
        .orElseThrow(() -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS));
  }

  /**
   * Returns all the saved users.
   *
   * @return a {@link java.util.List} of {@link UserDto}
   */
  @Override
  public AllUserResponse getAllUsers() {
    List<User> users = findAll();

    return new AllUserResponse(mapUsersToUserDtos(users));
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

  private List<UserDto> mapUsersToUserDtos(List<User> users) {
    return users.stream().map(UserDto::new).toList();
  }
}
