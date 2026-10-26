package dev.roland.inventory_management_backend.features.warehouse.service.impl;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseAddressDto;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseRequest;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseResponse;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.message.WarehouseMessageKey;
import dev.roland.inventory_management_backend.features.warehouse.repository.WarehouseRepository;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;
import dev.roland.inventory_management_backend.features.warehouse_address.WarehouseAddress;
import lombok.RequiredArgsConstructor;

/** Implements warehouse persistence and company-scoped configuration rules. */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
  private final WarehouseRepository warehouseRepository;

  @Override
  public JpaRepository<Warehouse, Long> getRepository() {
    return warehouseRepository;
  }

  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.WAREHOUSE;
  }

  @Override
  public List<WarehouseResponse> findAllByCompanyId(final Long companyId) {
    return warehouseRepository.findAllByCompanyIdOrderByNameAsc(companyId).stream()
        .map(WarehouseServiceImpl::toResponse)
        .toList();
  }

  @Override
  public Warehouse findEntityByIdAndCompanyId(final Long id, final Long companyId) {
    return findForCompany(id, companyId);
  }

  @Override
  public Warehouse findEntityByIdAndCompanyIdForUpdate(final Long id, final Long companyId) {
    return warehouseRepository
        .findByIdAndCompanyIdForUpdate(id, companyId)
        .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.WAREHOUSE));
  }

  @Transactional
  @Override
  public WarehouseResponse create(final Company company, final WarehouseRequest request) {
    validateUnique(company.getId(), request, null);
    final Warehouse warehouse =
        Warehouse.builder()
            .company(company)
            .name(request.getName().trim())
            .code(normalizeCode(request.getCode()))
            .type(request.getType())
            .status(WarehouseStatus.ACTIVE)
            .address(toAddress(request.getAddress()))
            .build();
    return toResponse(warehouseRepository.save(warehouse));
  }

  @Transactional
  @Override
  public WarehouseResponse update(
      final Long id, final Long companyId, final WarehouseRequest request) {
    final Warehouse warehouse = findForCompany(id, companyId);
    validateUnique(companyId, request, id);
    warehouse.setName(request.getName().trim());
    warehouse.setCode(normalizeCode(request.getCode()));
    warehouse.setType(request.getType());
    warehouse.setAddress(toAddress(request.getAddress()));
    return toResponse(warehouseRepository.save(warehouse));
  }

  @Transactional
  @Override
  public WarehouseResponse archive(final Warehouse warehouse) {
    if (warehouse.getStatus() == WarehouseStatus.ARCHIVED) {
      throw new ApiException(WarehouseMessageKey.WAREHOUSE_ALREADY_ARCHIVED);
    }
    warehouse.setStatus(WarehouseStatus.ARCHIVED);
    return toResponse(warehouseRepository.save(warehouse));
  }

  @Transactional
  @Override
  public WarehouseResponse restore(final Long id, final Long companyId) {
    final Warehouse warehouse = findForCompany(id, companyId);
    if (warehouse.getStatus() != WarehouseStatus.ARCHIVED) {
      throw new ApiException(WarehouseMessageKey.WAREHOUSE_NOT_ARCHIVED);
    }
    warehouse.setStatus(WarehouseStatus.ACTIVE);
    return toResponse(warehouseRepository.save(warehouse));
  }

  private Warehouse findForCompany(final Long id, final Long companyId) {
    return warehouseRepository
        .findByIdAndCompanyId(id, companyId)
        .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.WAREHOUSE));
  }

  private void validateUnique(
      final Long companyId, final WarehouseRequest request, final Long excludedId) {
    final String name = request.getName().trim();
    final String code = normalizeCode(request.getCode());
    final boolean nameExists =
        excludedId == null
            ? warehouseRepository.existsByCompanyIdAndName(companyId, name)
            : warehouseRepository.existsByCompanyIdAndNameAndIdNot(companyId, name, excludedId);
    if (nameExists) {
      throw new ApiException(WarehouseMessageKey.WAREHOUSE_NAME_ALREADY_EXISTS);
    }

    final boolean codeExists =
        excludedId == null
            ? warehouseRepository.existsByCompanyIdAndCode(companyId, code)
            : warehouseRepository.existsByCompanyIdAndCodeAndIdNot(companyId, code, excludedId);
    if (codeExists) {
      throw new ApiException(WarehouseMessageKey.WAREHOUSE_CODE_ALREADY_EXISTS);
    }
  }

  private static String normalizeCode(final String code) {
    return code.trim().toUpperCase(java.util.Locale.ROOT);
  }

  private static WarehouseAddress toAddress(final WarehouseAddressDto dto) {
    return dto == null
        ? null
        : WarehouseAddress.builder()
            .country(dto.getCountry().trim().toUpperCase(java.util.Locale.ROOT))
            .city(dto.getCity().trim())
            .addressLine1(dto.getAddressLine1().trim())
            .addressLine2(dto.getAddressLine2())
            .phone(dto.getPhone())
            .email(dto.getEmail())
            .build();
  }

  private static WarehouseResponse toResponse(final Warehouse warehouse) {
    final WarehouseAddress address = warehouse.getAddress();
    final WarehouseAddressDto addressDto =
        address == null
            ? null
            : new WarehouseAddressDto(
                address.getCountry(),
                address.getCity(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getPhone(),
                address.getEmail());
    return WarehouseResponse.builder()
        .id(warehouse.getId())
        .companyId(warehouse.getCompany().getId())
        .name(warehouse.getName())
        .code(warehouse.getCode())
        .type(warehouse.getType())
        .status(warehouse.getStatus())
        .address(addressDto)
        .createdAt(warehouse.getCreatedAt())
        .updatedAt(warehouse.getUpdatedAt())
        .build();
  }
}
