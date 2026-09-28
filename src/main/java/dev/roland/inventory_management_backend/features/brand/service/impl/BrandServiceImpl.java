package dev.roland.inventory_management_backend.features.brand.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.brand.Brand;
import dev.roland.inventory_management_backend.features.brand.repository.BrandRepository;
import dev.roland.inventory_management_backend.features.brand.service.BrandService;
import lombok.RequiredArgsConstructor;

/** Implements brand persistence operations. */
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
  private final BrandRepository brandRepository;

  @Override
  public List<Brand> search(final Long companyId, final String query) {
    return brandRepository.findByCompanyIdAndNameContainingIgnoreCaseOrderByNameAsc(
        companyId, query == null ? "" : query.trim());
  }

  @Override
  public Optional<Brand> findByIdAndCompanyId(final Long id, final Long companyId) {
    return brandRepository.findByIdAndCompanyId(id, companyId);
  }

  @Override
  public Brand save(final Brand brand) {
    return brandRepository.save(brand);
  }

  @Override
  public void delete(final Brand brand) {
    brandRepository.delete(brand);
  }

  @Override
  public boolean existsByCompanyIdAndNameIgnoreCase(final Long companyId, final String name) {
    return brandRepository.existsByCompanyIdAndNameIgnoreCase(companyId, name);
  }

  @Override
  public boolean existsByCompanyIdAndNameIgnoreCaseAndIdNot(
      final Long companyId, final String name, final Long id) {
    return brandRepository.existsByCompanyIdAndNameIgnoreCaseAndIdNot(companyId, name, id);
  }
}
