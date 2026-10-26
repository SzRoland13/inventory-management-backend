package dev.roland.inventory_management_backend.features.warehouse.service;

import java.util.List;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseRequest;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseResponse;

/** Defines company-scoped warehouse operations. */
public interface WarehouseService extends BaseService<Warehouse, Long> {
  /**
   * Lists warehouses belonging to a company.
   *
   * @param companyId company identifier
   * @return company warehouses
   */
  List<WarehouseResponse> findAllByCompanyId(Long companyId);

  /**
   * Finds a warehouse scoped to its company.
   *
   * @param id warehouse identifier
   * @param companyId company identifier
   * @return matching warehouse
   */
  Warehouse findEntityByIdAndCompanyId(Long id, Long companyId);

  /**
   * Finds and locks a company-owned warehouse.
   *
   * @param id warehouse identifier
   * @param companyId company identifier
   * @return locked warehouse
   */
  Warehouse findEntityByIdAndCompanyIdForUpdate(Long id, Long companyId);

  /**
   * Creates a warehouse for the supplied company.
   *
   * @param company owning company
   * @param request warehouse fields
   * @return created warehouse
   */
  WarehouseResponse create(Company company, WarehouseRequest request);

  /**
   * Updates a warehouse scoped to the supplied company.
   *
   * @param id warehouse identifier
   * @param companyId company identifier
   * @param request updated warehouse fields
   * @return updated warehouse
   */
  WarehouseResponse update(Long id, Long companyId, WarehouseRequest request);

  /**
   * Archives the supplied warehouse.
   *
   * @param warehouse warehouse to archive
   * @return archived warehouse
   */
  WarehouseResponse archive(Warehouse warehouse);

  /**
   * Restores a warehouse scoped to the supplied company.
   *
   * @param id warehouse identifier
   * @param companyId company identifier
   * @return restored warehouse
   */
  WarehouseResponse restore(Long id, Long companyId);
}
