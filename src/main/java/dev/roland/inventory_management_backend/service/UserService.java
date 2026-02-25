package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.AllUserResponse;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.exception.ApiException;
import dev.roland.inventory_management_backend.exception.NotFoundException;
import dev.roland.inventory_management_backend.model.User;
import org.springframework.security.core.Authentication;

public interface UserService extends BaseService<User, Long> {

    /**
     * Retrieves a {@link User} entity from the database that matches the given email address.
     *
     * @param email the email address to look up
     * @return the found {@link User},
     * or throws a {@link NotFoundException} if not found
     */
    User findUserByEmailOrThrow(String email);

    /**
     * Validates the current authenticated user session.
     *
     * @param auth the {@link Authentication} object automatically injected by Spring Security,
     *             representing the currently authenticated user
     * @throws ApiException if the authentication is missing, invalid, or the principal cannot be resolved
     */
    void checkSession(Authentication auth);

    /**
     * Returns all the saved users.
     *
     * @return a {@link java.util.List} of {@link UserDto}
     */
    AllUserResponse getAllUsers();

    /**
     * Updates the user with that data passed.
     *
     * @param id      - the id of the user to update
     * @param request - the data to update the user
     * @return the updated user in a {@link UserDto}
     */
    UserDto updateUser(Long id, AddEditUserRequest request);
}