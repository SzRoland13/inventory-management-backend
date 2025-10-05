package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    /**
     * Retrieves a {@link RefreshToken} entity from the database that matches the given token value.
     *
     * @param token the token string to look up
     * @return an {@link Optional} containing the found {@link RefreshToken},
     *         or an empty {@link Optional} if no matching token exists
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes the specified {@link RefreshToken} entity from the database.
     *
     * @param token the {@link RefreshToken} entity to delete
     */
    void delete(RefreshToken token);

    /**
     * Persists a new {@link RefreshToken} entity or updates an existing one in the database.
     *
     * @param refreshToken the {@link RefreshToken} entity to save or update
     * @return the saved or updated {@link RefreshToken} entity
     */
    RefreshToken save(RefreshToken refreshToken);
}
