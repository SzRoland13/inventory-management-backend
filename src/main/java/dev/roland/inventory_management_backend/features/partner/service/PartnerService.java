package dev.roland.inventory_management_backend.features.partner.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.features.partner.Partner;

/** Persistence operations for partners. */
public interface PartnerService {
  List<Partner> list(Long companyId, Boolean archived);

  Optional<Partner> find(Long id, Long companyId);

  Optional<Partner> findActive(Long id, Long companyId);

  boolean taxNumberExists(Long companyId, String taxNumber, Long exceptId);

  Partner save(Partner partner);
}
