package dev.roland.inventory_management_backend.features.partner.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.partner.Partner;

/** Queries partners within their owning company. */
public interface PartnerRepository extends JpaRepository<Partner, Long> {
  /**
   * Finds all partners belonging to the company, ordered by name.
   *
   * @param companyId owning company identifier
   * @return matching partners
   */
  List<Partner> findAllByCompanyIdOrderByNameAsc(Long companyId);

  /**
   * Finds active partners belonging to the company, ordered by name.
   *
   * @param companyId owning company identifier
   * @return matching active partners
   */
  List<Partner> findAllByCompanyIdAndDeletedAtIsNullOrderByNameAsc(Long companyId);

  /**
   * Finds archived partners belonging to the company, ordered by name.
   *
   * @param companyId owning company identifier
   * @return matching archived partners
   */
  List<Partner> findAllByCompanyIdAndDeletedAtIsNotNullOrderByNameAsc(Long companyId);

  /**
   * Finds a partner within its owning company.
   *
   * @param id partner identifier
   * @param companyId owning company identifier
   * @return matching partner, if it exists
   */
  Optional<Partner> findByIdAndCompanyId(Long id, Long companyId);

  /**
   * Finds an active partner within its owning company.
   *
   * @param id partner identifier
   * @param companyId owning company identifier
   * @return matching partner, if it exists
   */
  Optional<Partner> findByIdAndCompanyIdAndDeletedAtIsNull(Long id, Long companyId);

  /**
   * Checks for a tax number already used by a partner in the company.
   *
   * @param companyId owning company identifier
   * @param taxNumber tax number to check
   * @return whether the number is already in use
   */
  boolean existsByCompanyIdAndTaxNumber(Long companyId, String taxNumber);

  /**
   * Checks for a tax number used by another partner in the company.
   *
   * @param companyId owning company identifier
   * @param taxNumber tax number to check
   * @param id partner excluded from the check
   * @return whether another partner uses the number
   */
  boolean existsByCompanyIdAndTaxNumberAndIdNot(Long companyId, String taxNumber, Long id);
}
