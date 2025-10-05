package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.RefreshToken;
import dev.roland.inventory_management_backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Retrieves a {@link RefreshToken} entity from the database that matches the given token value.
     *
     * @param token the token string to look up
     * @return an {@link Optional} containing the found {@link RefreshToken},
     * or an empty {@link Optional} if no matching token exists
     */
    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Deletes the specified {@link RefreshToken} entity from the database.
     *
     * @param token the {@link RefreshToken} entity to delete
     */
    @Override
    public void delete(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }

    /**
     * Persists a new {@link RefreshToken} entity or updates an existing one in the database.
     *
     * @param refreshToken the {@link RefreshToken} entity to save or update
     * @return the saved or updated {@link RefreshToken} entity
     */
    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }
}
