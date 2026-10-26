package dev.roland.inventory_management_backend.features.partner.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.partner.Partner;
import dev.roland.inventory_management_backend.features.partner.repository.PartnerRepository;
import dev.roland.inventory_management_backend.features.partner.service.PartnerService;
import lombok.RequiredArgsConstructor;

/** Implements partner persistence. */
@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {
  private final PartnerRepository repository;

  @Override
  public List<Partner> list(Long companyId, Boolean archived) {
    if (Boolean.TRUE.equals(archived)) {
      return repository.findAllByCompanyIdAndDeletedAtIsNotNullOrderByNameAsc(companyId);
    } else if (Boolean.FALSE.equals(archived)) {
      return repository.findAllByCompanyIdAndDeletedAtIsNullOrderByNameAsc(companyId);
    } else {
      return repository.findAllByCompanyIdOrderByNameAsc(companyId);
    }
  }

  @Override
  public Optional<Partner> find(Long id, Long companyId) {
    return repository.findByIdAndCompanyId(id, companyId);
  }

  @Override
  public Optional<Partner> findActive(Long id, Long companyId) {
    return repository.findByIdAndCompanyIdAndDeletedAtIsNull(id, companyId);
  }

  @Override
  public boolean taxNumberExists(Long companyId, String taxNumber, Long exceptId) {
    return exceptId == null
        ? repository.existsByCompanyIdAndTaxNumber(companyId, taxNumber)
        : repository.existsByCompanyIdAndTaxNumberAndIdNot(companyId, taxNumber, exceptId);
  }

  @Override
  public Partner save(Partner partner) {
    return repository.save(partner);
  }
}
