package dev.roland.inventory_management_backend.features.brand.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.brand.Brand;
import dev.roland.inventory_management_backend.features.brand.dto.BrandRequest;
import dev.roland.inventory_management_backend.features.brand.message.BrandMessageKey;
import dev.roland.inventory_management_backend.features.brand.service.BrandService;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;

@ExtendWith(MockitoExtension.class)
class BrandFacadeImplTest {
  @Mock private BrandService brandService;
  @Mock private CompanyService companyService;

  private BrandFacadeImpl facade;
  private Company company;

  @BeforeEach
  void setUp() {
    facade = new BrandFacadeImpl(brandService, companyService);
    company = Company.builder().id(3L).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
  }

  @Test
  void searchesOnlyCurrentCompanyBrandsAndMapsResponses() {
    final Brand brand = brand(12L, "Acme");
    when(brandService.search(3L, "acm")).thenReturn(List.of(brand));

    final var results = facade.search("acm");

    assertEquals(1, results.size());
    assertEquals(12L, results.getFirst().id());
    assertEquals("Acme", results.getFirst().name());
    verify(brandService).search(3L, "acm");
  }

  @Test
  void createsTrimmedBrandOwnedByCurrentCompany() {
    when(brandService.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

    final var response = facade.create(new BrandRequest(" Acme "));

    assertEquals("Acme", response.name());
    verify(brandService)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                brand -> brand.getCompany() == company && brand.getName().equals("Acme")));
  }

  @Test
  void rejectsDuplicateBrandNamesIgnoringCase() {
    when(brandService.existsByCompanyIdAndNameIgnoreCase(3L, "ACME")).thenReturn(true);

    final ApiException exception =
        assertThrows(ApiException.class, () -> facade.create(new BrandRequest(" ACME ")));

    assertEquals(BrandMessageKey.BRAND_ALREADY_EXISTS, exception.getMessageKey());
    verify(brandService, never()).save(any(Brand.class));
  }

  @Test
  void updatesOnlyCurrentCompanyBrand() {
    final Brand existing = brand(12L, "Acme");
    when(brandService.findByIdAndCompanyId(12L, 3L)).thenReturn(Optional.of(existing));
    when(brandService.save(existing)).thenReturn(existing);

    final var response = facade.update(12L, new BrandRequest(" ACME Europe "));

    assertEquals("ACME Europe", response.name());
    verify(brandService).existsByCompanyIdAndNameIgnoreCaseAndIdNot(3L, "ACME Europe", 12L);
  }

  @Test
  void rejectsDuplicateUpdateAndMissingOrForeignBrand() {
    final Brand existing = brand(12L, "Acme");
    when(brandService.findByIdAndCompanyId(12L, 3L)).thenReturn(Optional.of(existing));
    when(brandService.existsByCompanyIdAndNameIgnoreCaseAndIdNot(3L, "Other", 12L))
        .thenReturn(true);
    final ApiException duplicate =
        assertThrows(ApiException.class, () -> facade.update(12L, new BrandRequest("Other")));
    assertEquals(BrandMessageKey.BRAND_ALREADY_EXISTS, duplicate.getMessageKey());

    when(brandService.findByIdAndCompanyId(13L, 3L)).thenReturn(Optional.empty());
    final NotFoundException missing =
        assertThrows(NotFoundException.class, () -> facade.delete(13L));
    assertEquals(NotFoundMessageKey.BRAND, missing.getMessageKey());
    verify(brandService, never()).delete(any(Brand.class));
    assertNull(existing.getCompany());
  }

  @Test
  void deletesBrandOwnedByCurrentCompany() {
    final Brand existing = brand(12L, "Acme");
    when(brandService.findByIdAndCompanyId(12L, 3L)).thenReturn(Optional.of(existing));

    facade.delete(12L);

    verify(brandService).delete(existing);
  }

  private Brand brand(final Long id, final String name) {
    final Brand brand = new Brand();
    brand.setId(id);
    brand.setName(name);
    return brand;
  }
}
