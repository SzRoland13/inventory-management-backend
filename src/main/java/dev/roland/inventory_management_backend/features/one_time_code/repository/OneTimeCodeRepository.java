package dev.roland.inventory_management_backend.features.one_time_code.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCode;

@Repository
public interface OneTimeCodeRepository extends JpaRepository<OneTimeCode, Long> {
  Optional<OneTimeCode> findByCode(String code);

  Optional<OneTimeCode> findByUserId(Long userId);
}
