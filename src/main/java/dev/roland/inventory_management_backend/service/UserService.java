package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.user.AllUserResponse;
import dev.roland.inventory_management_backend.dto.user.UpdateUserRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.messageKey.ApiException;
import dev.roland.inventory_management_backend.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface UserService {

    /**
     * Retrieves a {@link User} entity from the database that matches the given email address.
     *
     * @param email the email address to look up
     * @return an {@link Optional} containing the found {@link User},
     * or an empty {@link Optional} if no matching entity exists
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Persists a new {@link User} entity or updates an existing one in the database.
     *
     * @param user the {@link User} entity to save or update
     * @return the saved or updated {@link User} entity
     */
    User save(User user);

    /**
     * Retrieves a {@link User} entity from the database that matches the given id.
     *
     * @param userId the id to look up
     * @return an {@link Optional} containing the found {@link User},
     * or an empty {@link Optional} if no matching entity exists
     */
    Optional<User> findUserById(@NotNull Long userId);

    /**
     * Validates the current authenticated user session.
     *
     * @param auth the {@link Authentication} object automatically injected by Spring Security,
     *             representing the currently authenticated user
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with a success status
     *         if the session is valid
     * @throws ApiException if the authentication is missing, invalid, or the principal cannot be resolved
     */
    ResponseEntity<ApiResponse<Void>> checkSession(Authentication auth);

    ResponseEntity<ApiResponse<AllUserResponse>> getAllUsers();

    ResponseEntity<ApiResponse<UserDto>> updateUser(Long id, UpdateUserRequest updateRequest);
}
