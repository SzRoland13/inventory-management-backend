package dev.roland.inventory_management_backend.service;

import java.util.Optional;

import dev.roland.inventory_management_backend.model.Company;

public interface CompanyService extends BaseService<Company, Long> {
  Optional<Company> findFirstByOrderByIdAsc();
}
