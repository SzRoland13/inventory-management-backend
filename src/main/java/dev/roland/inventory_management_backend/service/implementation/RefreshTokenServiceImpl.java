package dev.roland.inventory_management_backend.service.implementation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.refresh_token.RefreshToken;
import dev.roland.inventory_management_backend.features.refresh_token.repository.RefreshTokenRepository;
import dev.roland.inventory_management_backend.features.refresh_token.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<RefreshToken, Long> getRepository() {
    return refreshTokenRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.REFRESH_TOKEN;
  }

  /**
   * Retrieves a {@link RefreshToken} entity from the database that matches the given token value.
   *
   * @param token the token string to look up
   * @return an {@link Optional} containing the found {@link RefreshToken}, or an empty {@link
   *     Optional} if no matching token exists
   */
  @Override
  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }
}
