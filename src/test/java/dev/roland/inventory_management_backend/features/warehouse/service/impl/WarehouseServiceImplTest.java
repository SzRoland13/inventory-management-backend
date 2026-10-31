package dev.roland.inventory_management_backend.features.warehouse.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseAddressDto;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseRequest;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import dev.roland.inventory_management_backend.features.warehouse.message.WarehouseMessageKey;
import dev.roland.inventory_management_backend.features.warehouse.repository.WarehouseRepository;
import dev.roland.inventory_management_backend.features.warehouse_address.WarehouseAddress;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {
  @Mock private WarehouseRepository warehouseRepository;

  private WarehouseServiceImpl service;
  private Company company;

  @BeforeEach
  void setUp() {
    service = new WarehouseServiceImpl(warehouseRepository);
    company = Company.builder().id(1L).build();
  }

  @Test
  void createsWarehouseWithNormalizedFieldsAndAddress() {
    final WarehouseAddressDto address =
        new WarehouseAddressDto("hu", "Budapest", "Main St", null, null, null);
    final WarehouseRequest request = request(" Main ", " wh-1 ", address);
    when(warehouseRepository.existsByCompanyIdAndName(1L, "Main")).thenReturn(false);
    when(warehouseRepository.existsByCompanyIdAndCode(1L, "WH-1")).thenReturn(false);
    when(warehouseRepository.save(any(Warehouse.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final var response = service.create(company, request);

    assertEquals("Main", response.getName());
    assertEquals("WH-1", response.getCode());
    assertEquals("HU", response.getAddress().getCountry());
    verify(warehouseRepository).save(any(Warehouse.class));
  }

  @Test
  void rejectsDuplicateWarehouseNameOrCode() {
    when(warehouseRepository.existsByCompanyIdAndName(1L, "Main")).thenReturn(true);
    final ApiException duplicateName =
        assertThrows(
            ApiException.class, () -> service.create(company, request("Main", "WH-1", null)));
    assertEquals(WarehouseMessageKey.WAREHOUSE_NAME_ALREADY_EXISTS, duplicateName.getMessageKey());

    when(warehouseRepository.existsByCompanyIdAndName(1L, "Main")).thenReturn(false);
    when(warehouseRepository.existsByCompanyIdAndCode(1L, "WH-1")).thenReturn(true);
    final ApiException duplicateCode =
        assertThrows(
            ApiException.class, () -> service.create(company, request("Main", "WH-1", null)));
    assertEquals(WarehouseMessageKey.WAREHOUSE_CODE_ALREADY_EXISTS, duplicateCode.getMessageKey());
    verify(warehouseRepository, never()).save(any(Warehouse.class));
  }

  @Test
  void updatesWarehouseExcludingItsExistingNameAndCodeFromUniquenessChecks() {
    final Warehouse warehouse = warehouse(9L, WarehouseStatus.ACTIVE, null);
    final WarehouseRequest request = request(" Updated ", " updated ", null);
    when(warehouseRepository.findByIdAndCompanyId(9L, 1L)).thenReturn(Optional.of(warehouse));
    when(warehouseRepository.existsByCompanyIdAndNameAndIdNot(1L, "Updated", 9L)).thenReturn(false);
    when(warehouseRepository.existsByCompanyIdAndCodeAndIdNot(1L, "UPDATED", 9L)).thenReturn(false);
    when(warehouseRepository.save(warehouse)).thenReturn(warehouse);

    final var response = service.update(9L, 1L, request);

    assertEquals("Updated", response.getName());
    assertEquals("UPDATED", response.getCode());
    assertNull(response.getAddress());
  }

  @Test
  void archivesOnlyActiveWarehouses() {
    final Warehouse active = warehouse(9L, WarehouseStatus.ACTIVE, null);
    when(warehouseRepository.save(active)).thenReturn(active);
    assertEquals(WarehouseStatus.ARCHIVED, service.archive(active).getStatus());

    final Warehouse archived = warehouse(9L, WarehouseStatus.ARCHIVED, null);
    final ApiException error = assertThrows(ApiException.class, () -> service.archive(archived));
    assertEquals(WarehouseMessageKey.WAREHOUSE_ALREADY_ARCHIVED, error.getMessageKey());
  }

  @Test
  void restoresOnlyArchivedWarehouses() {
    final Warehouse archived = warehouse(9L, WarehouseStatus.ARCHIVED, null);
    when(warehouseRepository.findByIdAndCompanyId(9L, 1L)).thenReturn(Optional.of(archived));
    when(warehouseRepository.save(archived)).thenReturn(archived);
    assertEquals(WarehouseStatus.ACTIVE, service.restore(9L, 1L).getStatus());

    final Warehouse active = warehouse(9L, WarehouseStatus.ACTIVE, null);
    when(warehouseRepository.findByIdAndCompanyId(10L, 1L)).thenReturn(Optional.of(active));
    final ApiException error = assertThrows(ApiException.class, () -> service.restore(10L, 1L));
    assertEquals(WarehouseMessageKey.WAREHOUSE_NOT_ARCHIVED, error.getMessageKey());
  }

  @Test
  void findsAndLocksWarehousesWithinTheirCompany() {
    final Warehouse warehouse = warehouse(9L, WarehouseStatus.ACTIVE, null);
    when(warehouseRepository.findByIdAndCompanyId(9L, 1L)).thenReturn(Optional.of(warehouse));
    when(warehouseRepository.findByIdAndCompanyIdForUpdate(9L, 1L))
        .thenReturn(Optional.of(warehouse));
    assertEquals(warehouse, service.findEntityByIdAndCompanyId(9L, 1L));
    assertEquals(warehouse, service.findEntityByIdAndCompanyIdForUpdate(9L, 1L));

    when(warehouseRepository.findByIdAndCompanyId(10L, 1L)).thenReturn(Optional.empty());
    when(warehouseRepository.findByIdAndCompanyIdForUpdate(10L, 1L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.findEntityByIdAndCompanyId(10L, 1L));
    assertThrows(
        NotFoundException.class, () -> service.findEntityByIdAndCompanyIdForUpdate(10L, 1L));
  }

  private WarehouseRequest request(
      final String name, final String code, final WarehouseAddressDto address) {
    final WarehouseRequest request = new WarehouseRequest();
    request.setName(name);
    request.setCode(code);
    request.setType(WarehouseType.STANDARD);
    request.setAddress(address);
    return request;
  }

  private Warehouse warehouse(
      final Long id, final WarehouseStatus status, final WarehouseAddress address) {
    return Warehouse.builder()
        .id(id)
        .company(company)
        .name("Main")
        .code("WH-1")
        .type(WarehouseType.STANDARD)
        .status(status)
        .address(address)
        .build();
  }
}
