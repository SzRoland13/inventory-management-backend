package dev.roland.inventory_management_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.model.RefreshToken;
import dev.roland.inventory_management_backend.model.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);

  void deleteByUser(User user);

  void deleteByToken(String token);
}
