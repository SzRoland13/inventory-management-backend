package dev.roland.inventory_management_backend.features.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.user.User;

/** Provides user lookup queries by username and email. */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  /**
   * Finds a user by username.
   *
   * @param username username to look up
   * @return matching user, if one exists
   */
  Optional<User> findByUsername(String username);

  /**
   * Finds a user by email address.
   *
   * @param email email address to look up
   * @return matching user, if one exists
   */
  Optional<User> findByEmail(String email);
}
