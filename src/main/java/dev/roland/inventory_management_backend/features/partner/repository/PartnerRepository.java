package dev.roland.inventory_management_backend.features.partner.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.partner.Partner;

/** Queries partners within their owning company. */
public interface PartnerRepository extends JpaRepository<Partner, Long> {
  List<Partner> findAllByCompanyIdOrderByNameAsc(Long companyId);

  List<Partner> findAllByCompanyIdAndDeletedAtIsNullOrderByNameAsc(Long companyId);

  List<Partner> findAllByCompanyIdAndDeletedAtIsNotNullOrderByNameAsc(Long companyId);

  Optional<Partner> findByIdAndCompanyId(Long id, Long companyId);

  Optional<Partner> findByIdAndCompanyIdAndDeletedAtIsNull(Long id, Long companyId);

  boolean existsByCompanyIdAndTaxNumber(Long companyId, String taxNumber);

  boolean existsByCompanyIdAndTaxNumberAndIdNot(Long companyId, String taxNumber, Long id);
}
