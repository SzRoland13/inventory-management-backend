package dev.roland.inventory_management_backend.features.brand.facade;

import java.util.List;

import dev.roland.inventory_management_backend.features.brand.dto.BrandRequest;
import dev.roland.inventory_management_backend.features.brand.dto.BrandResponse;

/** Coordinates company context and brand search. */
public interface BrandFacade {
  /**
   * Searches brands belonging to the current company.
   *
   * @param query substring to search for
   * @return matching brands
   */
  List<BrandResponse> search(String query);

  /**
   * Creates a brand for the current company.
   *
   * @param request brand data
   * @return created brand
   */
  BrandResponse create(BrandRequest request);

  /**
   * Updates a brand owned by the current company.
   *
   * @param id brand identifier
   * @param request updated brand data
   * @return updated brand
   */
  BrandResponse update(Long id, BrandRequest request);

  /**
   * Deletes a brand owned by the current company.
   *
   * @param id brand identifier
   */
  void delete(Long id);
}
