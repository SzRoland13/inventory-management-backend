package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataUpdateRequest;
import dev.roland.inventory_management_backend.dto.company.CompanyBillingDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyBillingDataUpdateRequest;
import dev.roland.inventory_management_backend.dto.company.CompanyExtendedResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyMinimalResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyPreferredCurrencyUpdateRequest;
import dev.roland.inventory_management_backend.dto.company.UpdatedPreferredCurrencyResponse;

/** Coordinates company data, logo, and preferred-currency operations. */
public interface CompanyFacade {
  /**
   * Returns the company data needed for minimal application views.
   *
   * @return minimal company data
   */
  CompanyMinimalResponse getMinimalCompanyData();

  /**
   * Returns the complete company profile and billing data.
   *
   * @return extended company data
   */
  CompanyExtendedResponse getExtendedCompanyData();

  /**
   * Updates the company's base profile data.
   *
   * @param request updated base company data
   * @return updated base company data
   */
  CompanyBaseDataResponse updateCompanyBaseData(CompanyBaseDataUpdateRequest request);

  /**
   * Associates a media asset with the company as its logo.
   *
   * @param mediaAssetId identifier of the media asset
   */
  void updateLogo(Long mediaAssetId);

  /**
   * Updates the company's billing data.
   *
   * @param request updated billing data
   * @return updated billing data
   */
  CompanyBillingDataResponse updateCompanyBillingData(CompanyBillingDataUpdateRequest request);

  /**
   * Updates the company's preferred currency.
   *
   * @param request preferred-currency update request
   * @return updated preferred-currency data
   */
  UpdatedPreferredCurrencyResponse updatePreferredCurrency(
      CompanyPreferredCurrencyUpdateRequest request);
}
