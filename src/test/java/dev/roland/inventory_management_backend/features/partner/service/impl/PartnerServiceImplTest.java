package dev.roland.inventory_management_backend.features.partner.service.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.features.partner.Partner;
import dev.roland.inventory_management_backend.features.partner.repository.PartnerRepository;

@ExtendWith(MockitoExtension.class)
class PartnerServiceImplTest {
  @Mock private PartnerRepository repository;
  @InjectMocks private PartnerServiceImpl service;

  @Test
  void listsPartnersForEachArchiveFilter() {
    final List<Partner> partners = List.of(org.mockito.Mockito.mock(Partner.class));
    when(repository.findAllByCompanyIdAndDeletedAtIsNotNullOrderByNameAsc(1L)).thenReturn(partners);
    when(repository.findAllByCompanyIdAndDeletedAtIsNullOrderByNameAsc(1L)).thenReturn(partners);
    when(repository.findAllByCompanyIdOrderByNameAsc(1L)).thenReturn(partners);

    assertSame(partners, service.list(1L, true));
    assertSame(partners, service.list(1L, false));
    assertSame(partners, service.list(1L, null));
  }

  @Test
  void checksTaxNumberWithOptionalExcludedPartner() {
    when(repository.existsByCompanyIdAndTaxNumber(1L, "TAX")).thenReturn(true);
    when(repository.existsByCompanyIdAndTaxNumberAndIdNot(1L, "TAX", 2L)).thenReturn(false);

    org.junit.jupiter.api.Assertions.assertTrue(service.taxNumberExists(1L, "TAX", null));
    org.junit.jupiter.api.Assertions.assertFalse(service.taxNumberExists(1L, "TAX", 2L));
  }
}
