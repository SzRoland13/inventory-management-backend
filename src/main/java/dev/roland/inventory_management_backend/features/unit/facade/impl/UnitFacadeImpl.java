package dev.roland.inventory_management_backend.features.unit.facade.impl;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.product.dto.UnitRequest;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.product.mapper.ProductSettingsMapper;
import dev.roland.inventory_management_backend.features.product.message.ProductMessageKey;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.facade.UnitFacade;
import dev.roland.inventory_management_backend.features.unit.service.UnitService;
import lombok.RequiredArgsConstructor;

/** Implements unit catalog operations. */
@Service
@RequiredArgsConstructor
public class UnitFacadeImpl implements UnitFacade {
  private final UnitService unitService;
  private final ProductService productService;
  private final DocumentLineService documentLineService;
  private final CompanyService companyService;
  private final ProductSettingsMapper mapper;

  @Override
  @Transactional
  public List<UnitResponse> list() {
    return unitService.findSelectableUnits(companyId()).stream()
        .map(mapper::toUnitResponse)
        .toList();
  }

  @Override
  @Transactional
  public UnitResponse create(final UnitRequest request) {
    final var company = companyService.getCompanyOrCreateNew();
    if (unitService.existsByCompanyIdAndCode(company.getId(), request.code().trim())) {
      throw invalid();
    }
    final Unit unit =
        Unit.builder()
            .code(request.code().trim())
            .name(request.name().trim())
            .symbol(request.symbol().trim())
            .system(false)
            .company(company)
            .build();
    return mapper.toUnitResponse(unitService.save(unit));
  }

  @Override
  @Transactional
  public UnitResponse update(final Long id, final UnitRequest request) {
    final Unit unit = findCompanyUnit(id);
    final String code = request.code().trim();
    if (!unit.getCode().equals(code) && unitService.existsByCompanyIdAndCode(companyId(), code)) {
      throw invalid();
    }
    unit.setCode(code);
    unit.setName(request.name().trim());
    unit.setSymbol(request.symbol().trim());
    return mapper.toUnitResponse(unitService.save(unit));
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    final Unit unit = findCompanyUnit(id);
    if (productService.existsUsingUnit(id) || documentLineService.existsByUnitSnapshotId(id)) {
      throw new ApiException(ProductMessageKey.PRODUCT_UNIT_IN_USE);
    }
    unitService.delete(unit);
  }

  private Unit findCompanyUnit(final Long id) {
    final Unit unit = unitService.findById(id).orElseThrow(this::invalid);
    if (unit.isSystem()
        || unit.getCompany() == null
        || !unit.getCompany().getId().equals(companyId())) {
      throw invalid();
    }
    return unit;
  }

  private Long companyId() {
    return companyService.getCompanyOrCreateNew().getId();
  }

  private ApiException invalid() {
    return new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA);
  }
}
