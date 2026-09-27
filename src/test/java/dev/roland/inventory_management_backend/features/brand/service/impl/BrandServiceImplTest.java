package dev.roland.inventory_management_backend.features.brand.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.features.brand.Brand;
import dev.roland.inventory_management_backend.features.brand.repository.BrandRepository;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {
  @Mock private BrandRepository brandRepository;

  @Test
  void delegatesCompanySearchWithTrimmedQuery() {
    final BrandServiceImpl service = new BrandServiceImpl(brandRepository);
    final List<Brand> brands = List.of(new Brand());
    when(brandRepository.findByCompanyIdAndNameContainingIgnoreCaseOrderByNameAsc(4L, "acm"))
        .thenReturn(brands);

    assertEquals(brands, service.search(4L, " acm "));
    verify(brandRepository).findByCompanyIdAndNameContainingIgnoreCaseOrderByNameAsc(4L, "acm");
  }

  @Test
  void usesEmptyQueryWhenSearchQueryIsNull() {
    final BrandServiceImpl service = new BrandServiceImpl(brandRepository);
    when(brandRepository.findByCompanyIdAndNameContainingIgnoreCaseOrderByNameAsc(4L, ""))
        .thenReturn(List.of());

    assertEquals(List.of(), service.search(4L, null));
  }

  @Test
  void delegatesScopedLookupSaveDeleteAndDuplicateChecks() {
    final BrandServiceImpl service = new BrandServiceImpl(brandRepository);
    final Brand brand = new Brand();
    when(brandRepository.findByIdAndCompanyId(5L, 4L)).thenReturn(Optional.of(brand));
    when(brandRepository.save(brand)).thenReturn(brand);
    when(brandRepository.existsByCompanyIdAndNameIgnoreCase(4L, "Acme")).thenReturn(true);
    when(brandRepository.existsByCompanyIdAndNameIgnoreCase(4L, "Missing")).thenReturn(false);
    when(brandRepository.existsByCompanyIdAndNameIgnoreCaseAndIdNot(4L, "Acme", 5L))
        .thenReturn(true);
    when(brandRepository.existsByCompanyIdAndNameIgnoreCaseAndIdNot(4L, "Missing", 5L))
        .thenReturn(false);

    assertEquals(Optional.of(brand), service.findByIdAndCompanyId(5L, 4L));
    assertEquals(brand, service.save(brand));
    assertTrue(service.existsByCompanyIdAndNameIgnoreCase(4L, "Acme"));
    assertFalse(service.existsByCompanyIdAndNameIgnoreCase(4L, "Missing"));
    assertTrue(service.existsByCompanyIdAndNameIgnoreCaseAndIdNot(4L, "Acme", 5L));
    assertFalse(service.existsByCompanyIdAndNameIgnoreCaseAndIdNot(4L, "Missing", 5L));
    service.delete(brand);

    verify(brandRepository).delete(brand);
  }
}
