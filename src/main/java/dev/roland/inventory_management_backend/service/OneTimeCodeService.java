package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.OneTimeCode;

import java.util.Optional;

public interface OneTimeCodeService {

    /**
     * Persists a new {@link OneTimeCode} entity or updates an existing one in the database.
     *
     * @param oneTimeCode the {@link OneTimeCode} entity to save or update
     * @return the saved or updated {@link OneTimeCode} entity
     */
    OneTimeCode save(OneTimeCode oneTimeCode);

    /**
     * Retrieves a {@link OneTimeCode} entity from the database that matches the given code value.
     *
     * @param code the one time code string to look up
     * @return an {@link Optional} containing the found {@link OneTimeCode},
     * or an empty {@link Optional} if no matching token exists
     */
    Optional<OneTimeCode> findByCode(String code);

    /**
     * Deletes the specified {@link OneTimeCode} entity from the database.
     *
     * @param oneTimeCode the {@link OneTimeCode} entity to delete
     */
    void delete(OneTimeCode oneTimeCode);

    /**
     * Retrieves a {@link OneTimeCode} entity from the database that matches the given user id.
     *
     * @param userId the one time code string to look up
     * @return an {@link Optional} containing the found {@link OneTimeCode},
     * or an empty {@link Optional} if no matching token exists
     */
    Optional<OneTimeCode> findByUserId(Long userId);
}
