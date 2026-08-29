package dev.roland.inventory_management_backend.features.company.service;

import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.company.Company;

public interface CompanyService extends BaseService<Company, Long> {
  /**
   * Finds the first configured company ordered by id.
   *
   * @return optional company when one exists
   */
  Optional<Company> findFirstByOrderByIdAsc();

  /**
   * Returns the existing company or creates the initial placeholder company.
   *
   * @return existing or newly created company
   */
  Company getCompanyOrCreateNew();
}
