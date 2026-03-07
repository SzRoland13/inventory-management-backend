package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyExtendedResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyUpdateRequest;

public interface CompanyFacade {
  CompanyBaseDataResponse getBaseCompanyData();

  CompanyExtendedResponse getExtendedCompanyData();

  CompanyExtendedResponse updateCompany(CompanyUpdateRequest request);

  void updateLogo(Long mediaAssetId);
}
