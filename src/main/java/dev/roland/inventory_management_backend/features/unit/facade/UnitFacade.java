package dev.roland.inventory_management_backend.features.unit.facade;

import java.util.List;

import dev.roland.inventory_management_backend.features.product.dto.UnitRequest;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;

/** Company-scoped unit catalog operations. */
public interface UnitFacade {
  /**
   * Lists the current company's selectable units.
   *
   * @return selectable units
   */
  List<UnitResponse> list();

  /**
   * Creates a company-owned unit.
   *
   * @param request unit data
   * @return created unit
   */
  UnitResponse create(UnitRequest request);

  /**
   * Updates a company-owned unit.
   *
   * @param id unit identifier
   * @param request updated unit data
   * @return updated unit
   */
  UnitResponse update(Long id, UnitRequest request);

  /**
   * Deletes a company-owned unit when it is unused.
   *
   * @param id unit identifier
   */
  void delete(Long id);
}
