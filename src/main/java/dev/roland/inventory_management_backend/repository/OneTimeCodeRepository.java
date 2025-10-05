package dev.roland.inventory_management_backend.repository;

import dev.roland.inventory_management_backend.model.OneTimeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OneTimeCodeRepository extends JpaRepository<OneTimeCode, Long> {
    Optional<OneTimeCode> findByCode(String code);
}
