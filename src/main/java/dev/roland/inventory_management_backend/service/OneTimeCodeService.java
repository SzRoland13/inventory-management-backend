package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.OneTimeCode;

import java.util.Optional;

public interface OneTimeCodeService extends BaseService<OneTimeCode, Long> {

    /**
     * Retrieves a {@link OneTimeCode} entity from the database that matches the given code value.
     *
     * @param code the one time code string to look up
     * @return an {@link Optional} containing the found {@link OneTimeCode},
     * or an empty {@link Optional} if no matching token exists
     */
    Optional<OneTimeCode> findByCode(String code);

    /**
     * Retrieves a {@link OneTimeCode} entity from the database that matches the given user id.
     *
     * @param userId the one time code string to look up
     * @return an {@link Optional} containing the found {@link OneTimeCode},
     * or an empty {@link Optional} if no matching token exists
     */
    Optional<OneTimeCode> findByUserId(Long userId);
}
