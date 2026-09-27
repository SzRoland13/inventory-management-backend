package dev.roland.inventory_management_backend.features.brand.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.features.brand.Brand;

/** Defines persistence operations for company brands. */
public interface BrandService {
  /**
   * Finds company brands whose names contain the supplied query.
   *
   * @param companyId company identifier
   * @param query name substring
   * @return matching brands
   */
  List<Brand> search(Long companyId, String query);

  /**
   * Finds a brand owned by the supplied company.
   *
   * @param id brand identifier
   * @param companyId company identifier
   * @return the brand when found
   */
  Optional<Brand> findByIdAndCompanyId(Long id, Long companyId);

  /**
   * Saves a brand.
   *
   * @param brand brand to save
   * @return saved brand
   */
  Brand save(Brand brand);

  /**
   * Deletes a brand.
   *
   * @param brand brand to delete
   */
  void delete(Brand brand);

  /**
   * Checks for a case-insensitive company brand name match.
   *
   * @param companyId company identifier
   * @param name brand name
   * @return true when a matching brand exists
   */
  boolean existsByCompanyIdAndNameIgnoreCase(Long companyId, String name);

  /**
   * Checks for a case-insensitive name match excluding one brand.
   *
   * @param companyId company identifier
   * @param name brand name
   * @param id brand to exclude
   * @return true when another matching brand exists
   */
  boolean existsByCompanyIdAndNameIgnoreCaseAndIdNot(Long companyId, String name, Long id);
}
