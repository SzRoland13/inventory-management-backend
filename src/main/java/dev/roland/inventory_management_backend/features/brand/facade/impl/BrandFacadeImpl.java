package dev.roland.inventory_management_backend.features.brand.facade.impl;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.brand.Brand;
import dev.roland.inventory_management_backend.features.brand.dto.BrandRequest;
import dev.roland.inventory_management_backend.features.brand.dto.BrandResponse;
import dev.roland.inventory_management_backend.features.brand.facade.BrandFacade;
import dev.roland.inventory_management_backend.features.brand.message.BrandMessageKey;
import dev.roland.inventory_management_backend.features.brand.service.BrandService;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import lombok.RequiredArgsConstructor;

/** Implements company-scoped brand search orchestration. */
@Service
@RequiredArgsConstructor
public class BrandFacadeImpl implements BrandFacade {
  private final BrandService brandService;
  private final CompanyService companyService;

  @Override
  public List<BrandResponse> search(final String query) {
    final Long companyId = companyService.getCompanyOrCreateNew().getId();
    return brandService.search(companyId, query).stream()
        .map(brand -> new BrandResponse(brand.getId(), brand.getName()))
        .toList();
  }

  @Transactional
  @Override
  public BrandResponse create(final BrandRequest request) {
    final var company = companyService.getCompanyOrCreateNew();
    final String name = request.name().trim();
    if (brandService.existsByCompanyIdAndNameIgnoreCase(company.getId(), name)) {
      throw new ApiException(BrandMessageKey.BRAND_ALREADY_EXISTS);
    }
    final Brand brand = new Brand();
    brand.setName(name);
    brand.setCompany(company);
    return toResponse(brandService.save(brand));
  }

  @Transactional
  @Override
  public BrandResponse update(final Long id, final BrandRequest request) {
    final var company = companyService.getCompanyOrCreateNew();
    final Brand brand = findBrand(id, company.getId());
    final String name = request.name().trim();
    if (brandService.existsByCompanyIdAndNameIgnoreCaseAndIdNot(company.getId(), name, id)) {
      throw new ApiException(BrandMessageKey.BRAND_ALREADY_EXISTS);
    }
    brand.setName(name);
    return toResponse(brandService.save(brand));
  }

  @Transactional
  @Override
  public void delete(final Long id) {
    final Long companyId = companyService.getCompanyOrCreateNew().getId();
    brandService.delete(findBrand(id, companyId));
  }

  private Brand findBrand(final Long id, final Long companyId) {
    return brandService
        .findByIdAndCompanyId(id, companyId)
        .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.BRAND));
  }

  private BrandResponse toResponse(final Brand brand) {
    return new BrandResponse(brand.getId(), brand.getName());
  }
}
