package dev.roland.inventory_management_backend.features.company.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.company.Company;

/** Provides database access to company profile records. */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
  /**
   * Finds the first company record by id.
   *
   * @return company with the lowest identifier, if one exists
   */
  Optional<Company> findFirstByOrderByIdAsc();
}
