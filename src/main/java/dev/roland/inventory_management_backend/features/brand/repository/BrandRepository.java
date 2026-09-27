package dev.roland.inventory_management_backend.features.brand.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.brand.Brand;

/** Database queries for company brands. */
public interface BrandRepository extends JpaRepository<Brand, Long> {
  /**
   * Searches a company's brands by a case-insensitive substring, ordered by name.
   *
   * @param companyId company identifier
   * @param name search substring
   * @return matching brands
   */
  List<Brand> findByCompanyIdAndNameContainingIgnoreCaseOrderByNameAsc(Long companyId, String name);

  /**
   * Finds a brand only when it belongs to the supplied company.
   *
   * @param id brand identifier
   * @param companyId company identifier
   * @return the brand when found
   */
  Optional<Brand> findByIdAndCompanyId(Long id, Long companyId);

  /**
   * Finds a company brand by a case-insensitive exact name.
   *
   * @param companyId company identifier
   * @param name brand name
   * @return the matching brand
   */
  Optional<Brand> findByCompanyIdAndNameIgnoreCase(Long companyId, String name);

  /**
   * Checks whether a company already has a brand with this name, ignoring case.
   *
   * @param companyId company identifier
   * @param name brand name
   * @return true when a matching brand exists
   */
  boolean existsByCompanyIdAndNameIgnoreCase(Long companyId, String name);

  /**
   * Checks for a case-insensitive name collision excluding one brand.
   *
   * @param companyId company identifier
   * @param name brand name
   * @param id brand to exclude
   * @return true when another matching brand exists
   */
  boolean existsByCompanyIdAndNameIgnoreCaseAndIdNot(Long companyId, String name, Long id);
}
