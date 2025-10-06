package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.User;
import jakarta.validation.constraints.NotNull;

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
}
