package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyExtendedResponse;

public interface CompanyFacade {
  CompanyBaseDataResponse getBaseCompanyData();

  CompanyExtendedResponse getExtendedCompanyData();
}
