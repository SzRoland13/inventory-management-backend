package dev.roland.inventory_management_backend.service;

import java.util.Optional;

import dev.roland.inventory_management_backend.model.RefreshToken;

public interface RefreshTokenService extends BaseService<RefreshToken, Long> {

  /**
   * Retrieves a {@link RefreshToken} entity from the database that matches the given token value.
   *
   * @param token the token string to look up
   * @return an {@link Optional} containing the found {@link RefreshToken}, or an empty {@link
   *     Optional} if no matching token exists
   */
  Optional<RefreshToken> findByToken(String token);
}
