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
  public List<Partner> list(final Long companyId, final Boolean archived) {
    final List<Partner> partners;
    if (Boolean.TRUE.equals(archived)) {
      partners = repository.findAllByCompanyIdAndDeletedAtIsNotNullOrderByNameAsc(companyId);
    } else if (Boolean.FALSE.equals(archived)) {
      partners = repository.findAllByCompanyIdAndDeletedAtIsNullOrderByNameAsc(companyId);
    } else {
      partners = repository.findAllByCompanyIdOrderByNameAsc(companyId);
    }
    return partners;
  }

  @Override
  public Optional<Partner> find(final Long id, final Long companyId) {
    return repository.findByIdAndCompanyId(id, companyId);
  }

  @Override
  public Optional<Partner> findActive(final Long id, final Long companyId) {
    return repository.findByIdAndCompanyIdAndDeletedAtIsNull(id, companyId);
  }

  @Override
  public boolean taxNumberExists(
      final Long companyId, final String taxNumber, final Long exceptId) {
    final boolean exists;
    if (exceptId == null) {
      exists = repository.existsByCompanyIdAndTaxNumber(companyId, taxNumber);
    } else {
      exists = repository.existsByCompanyIdAndTaxNumberAndIdNot(companyId, taxNumber, exceptId);
    }
    return exists;
  }

  @Override
  public Partner save(final Partner partner) {
    return repository.save(partner);
  }
}
