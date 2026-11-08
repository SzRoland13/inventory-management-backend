package dev.roland.inventory_management_backend.features.one_time_code.service;

import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCode;

/** Defines operations supported by the one-time setup token feature. */
public interface OneTimeCodeService extends BaseService<OneTimeCode, Long> {

  /**
   * Locks and retrieves a {@link OneTimeCode} entity matching the given token hash.
   *
   * @param codeHash SHA-256 hash of the raw token
   * @return an {@link Optional} containing the found {@link OneTimeCode}, or an empty {@link
   *     Optional} if no matching token exists
   */
  Optional<OneTimeCode> findByCodeHashForUpdate(String codeHash);

  /**
   * Retrieves a {@link OneTimeCode} entity from the database that matches the given user id.
   *
   * @param userId user id to look up
   * @return an {@link Optional} containing the found {@link OneTimeCode}, or an empty {@link
   *     Optional} if no matching token exists
   */
  Optional<OneTimeCode> findByUserId(Long userId);

  /**
   * Deletes a user's current token immediately so a replacement can be inserted safely.
   *
   * @param userId identifier of the user
   */
  void deleteByUserId(Long userId);
}
