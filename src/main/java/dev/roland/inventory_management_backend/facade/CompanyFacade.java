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
   * <p>minimal company data
   */
  CompanyMinimalResponse getMinimalCompanyData();

  /**
   * Returns the complete company profile and billing data.
   *
   * <p>extended company data
   */
  CompanyExtendedResponse getExtendedCompanyData();

  /**
   * Updates the company's base profile data.
   *
   * <p>request updated base company data updated base company data
   */
  CompanyBaseDataResponse updateCompanyBaseData(CompanyBaseDataUpdateRequest request);

  /**
   * Associates a media asset with the company as its logo.
   *
   * <p>mediaAssetId identifier of the media asset
   */
  void updateLogo(Long mediaAssetId);

  /**
   * Updates the company's billing data.
   *
   * <p>request updated billing data updated billing data
   */
  CompanyBillingDataResponse updateCompanyBillingData(CompanyBillingDataUpdateRequest request);

  /**
   * Updates the company's preferred currency.
   *
   * <p>request preferred-currency update request updated preferred-currency data
   */
  UpdatedPreferredCurrencyResponse updatePreferredCurrency(
      CompanyPreferredCurrencyUpdateRequest request);
}
