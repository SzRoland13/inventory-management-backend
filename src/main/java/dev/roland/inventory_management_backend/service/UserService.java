package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.exception.NotFoundException;
import dev.roland.inventory_management_backend.model.User;

public interface UserService extends BaseService<User, Long> {

  /**
   * Retrieves a {@link User} entity from the database that matches the given email address.
   *
   * @param email the email address to look up
   * @return the found {@link User}, or throws a {@link NotFoundException} if not found
   */
  User findUserByEmailOrThrow(String email);

  /**
   * Retrieves a {@link User} entity from the database that matches the given username.
   *
   * @param username the username to look up
   * @return the found {@link User}, or throws a {@link NotFoundException} if not found
   */
  User findByUsernameOrThrow(String username);

  /**
   * Updates the user with that data passed.
   *
   * @param id - the id of the user to update
   * @param request - the data to update the user
   * @return the updated user in a {@link UserDto}
   */
  UserDto updateUser(Long id, AddEditUserRequest request);
}
