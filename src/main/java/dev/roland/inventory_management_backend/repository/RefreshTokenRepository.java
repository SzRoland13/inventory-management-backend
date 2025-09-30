package dev.roland.inventory_management_backend.repository;

import dev.roland.inventory_management_backend.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, Long> {
}
