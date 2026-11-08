package dev.roland.inventory_management_backend.features.one_time_code.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCode;

/** Provides queries for one-time setup tokens by hash and user. */
@Repository
public interface OneTimeCodeRepository extends JpaRepository<OneTimeCode, Long> {
  /**
   * Locks and finds a one-time code by its stored hash.
   *
   * @param codeHash SHA-256 hash of the raw token
   * @return matching code, if one exists
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select otc from OneTimeCode otc where otc.codeHash = :codeHash")
  Optional<OneTimeCode> findByCodeHashForUpdate(@Param("codeHash") String codeHash);

  /**
   * Finds the one-time code associated with a user.
   *
   * @param userId identifier of the user
   * @return matching code, if one exists
   */
  Optional<OneTimeCode> findByUserId(Long userId);
}
