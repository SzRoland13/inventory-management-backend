package dev.roland.inventory_management_backend.features.warehouse.facade;

import java.util.List;

import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseRequest;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseResponse;

/** Coordinates company, warehouse, and stock-balance operations. */
public interface WarehouseFacade {
  /**
   * Lists warehouses for the current company.
   *
   * @return company warehouses
   */
  List<WarehouseResponse> findAllForCurrentCompany();

  /**
   * Creates a warehouse for the current company.
   *
   * @param request warehouse fields
   * @return created warehouse
   */
  WarehouseResponse create(WarehouseRequest request);

  /**
   * Updates a company-owned warehouse.
   *
   * @param id warehouse identifier
   * @param request updated warehouse fields
   * @return updated warehouse
   */
  WarehouseResponse update(Long id, WarehouseRequest request);

  /**
   * Archives a warehouse after confirming its balances are zero.
   *
   * @param id warehouse identifier
   * @return archived warehouse
   */
  WarehouseResponse archive(Long id);

  /**
   * Restores an archived warehouse.
   *
   * @param id warehouse identifier
   * @return restored warehouse
   */
  WarehouseResponse restore(Long id);
}
