package dev.roland.inventory_management_backend.features.partner.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.features.partner.Partner;

/** Persistence operations for partners. */
public interface PartnerService {
  /**
   * Lists company partners, optionally filtered by archive state.
   *
   * @param companyId owning company identifier
   * @param archived optional archive filter
   * @return matching partners
   */
  List<Partner> list(Long companyId, Boolean archived);

  /**
   * Finds a partner within its owning company.
   *
   * @param id partner identifier
   * @param companyId owning company identifier
   * @return matching partner, if it exists
   */
  Optional<Partner> find(Long id, Long companyId);

  /**
   * Finds an active partner within its owning company.
   *
   * @param id partner identifier
   * @param companyId owning company identifier
   * @return matching active partner, if it exists
   */
  Optional<Partner> findActive(Long id, Long companyId);

  /**
   * Checks whether a company already uses a tax number.
   *
   * @param companyId owning company identifier
   * @param taxNumber tax number to check
   * @param exceptId partner excluded from the check
   * @return whether another partner uses the number
   */
  boolean taxNumberExists(Long companyId, String taxNumber, Long exceptId);

  /**
   * Saves a partner.
   *
   * @param partner partner to persist
   * @return saved partner
   */
  Partner save(Partner partner);
}
