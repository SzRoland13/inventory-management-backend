package dev.roland.inventory_management_backend.features.company.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.company.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
  Optional<Company> findFirstByOrderByIdAsc();
}
