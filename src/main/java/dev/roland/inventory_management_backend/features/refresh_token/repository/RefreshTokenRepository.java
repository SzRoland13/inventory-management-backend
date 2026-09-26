package dev.roland.inventory_management_backend.features.refresh_token.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.refresh_token.RefreshToken;
import dev.roland.inventory_management_backend.features.user.User;

/** Provides lookup and deletion queries for persisted refresh tokens. */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  /**
   * Finds a refresh token by its token value.
   *
   * @param token token value to look up
   * @return matching token, if one exists
   */
  Optional<RefreshToken> findByToken(String token);

  /**
   * Deletes refresh tokens associated with a user.
   *
   * @param user owner of the tokens to delete
   */
  void deleteByUser(User user);

  /**
   * Deletes the refresh token with the supplied value.
   *
   * @param token token value to delete
   */
  void deleteByToken(String token);
}
