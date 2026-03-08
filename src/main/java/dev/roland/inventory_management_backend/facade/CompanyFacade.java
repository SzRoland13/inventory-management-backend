package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataUpdateRequest;
import dev.roland.inventory_management_backend.dto.company.CompanyBillingDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyBillingDataUpdateRequest;
import dev.roland.inventory_management_backend.dto.company.CompanyExtendedResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyMinimalResponse;

public interface CompanyFacade {
  CompanyMinimalResponse getMinimalCompanyData();

  CompanyExtendedResponse getExtendedCompanyData();

  CompanyBaseDataResponse updateCompanyBaseData(CompanyBaseDataUpdateRequest request);

  void updateLogo(Long mediaAssetId);

  CompanyBillingDataResponse updateCompanyBillingData(CompanyBillingDataUpdateRequest request);
}
