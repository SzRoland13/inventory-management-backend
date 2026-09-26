package dev.roland.inventory_management_backend.features.company;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.common.message.GenericMessageKey;
import dev.roland.inventory_management_backend.features.company.dto.CompanyBaseDataResponse;
import dev.roland.inventory_management_backend.features.company.dto.CompanyBaseDataUpdateRequest;
import dev.roland.inventory_management_backend.features.company.dto.CompanyBillingDataResponse;
import dev.roland.inventory_management_backend.features.company.dto.CompanyBillingDataUpdateRequest;
import dev.roland.inventory_management_backend.features.company.dto.CompanyExtendedResponse;
import dev.roland.inventory_management_backend.features.company.dto.CompanyMinimalResponse;
import dev.roland.inventory_management_backend.features.company.dto.CompanyPreferredCurrencyUpdateRequest;
import dev.roland.inventory_management_backend.features.company.dto.LogoUpdateRequest;
import dev.roland.inventory_management_backend.features.company.dto.UpdatedPreferredCurrencyResponse;
import dev.roland.inventory_management_backend.features.company.facade.CompanyFacade;
import dev.roland.inventory_management_backend.features.company.message.CompanyMessageKey;
import lombok.RequiredArgsConstructor;

/** Exposes company profile and billing-data REST endpoints. */
@RestController
@RequestMapping(CompanyController.COMPANY_BASE_ENDPOINT)
@RequiredArgsConstructor
public class CompanyController {
  public static final String COMPANY_BASE_ENDPOINT = "api/v1/company";
  public static final String COMPANY_EXTENDED_ENDPOINT = "/extended";
  public static final String LOGO_ENDPOINT = "/logo";
  public static final String BILLING_ENDPOINT = "/billing";
  public static final String PREFERRED_CURRENCY_ENDPOINT = "/currency";

  private final CompanyFacade companyFacade;

  /**
   * Returns the company details used in compact views.
   *
   * @return response containing the minimal company profile
   */
  @GetMapping
  public ResponseEntity<ApiResponse<CompanyMinimalResponse>> getMinimalCompanyData() {
    return ResponseEntity.ok(
        ApiResponse.success(
            GenericMessageKey.REQUEST_SUCCESS, companyFacade.getMinimalCompanyData()));
  }

  /**
   * Returns the full company profile.
   *
   * @return response containing the extended company profile
   */
  @GetMapping(COMPANY_EXTENDED_ENDPOINT)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<CompanyExtendedResponse>> getExtendedCompanyData() {
    return ResponseEntity.ok(
        ApiResponse.success(
            GenericMessageKey.REQUEST_SUCCESS, companyFacade.getExtendedCompanyData()));
  }

  /**
   * Updates the company profile details.
   *
   * @param request company details to save
   * @return response containing the updated profile
   */
  @PutMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<CompanyBaseDataResponse>> updateCompanyBaseData(
      @RequestBody CompanyBaseDataUpdateRequest request) {

    return ResponseEntity.ok(
        ApiResponse.success(
            GenericMessageKey.REQUEST_SUCCESS, companyFacade.updateCompanyBaseData(request)));
  }

  /**
   * Updates the company billing details.
   *
   * @param request billing details to save
   * @return response containing the updated billing profile
   */
  @PutMapping(BILLING_ENDPOINT)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<CompanyBillingDataResponse>> updateCompanyBillingData(
      @RequestBody CompanyBillingDataUpdateRequest request) {

    return ResponseEntity.ok(
        ApiResponse.success(
            GenericMessageKey.REQUEST_SUCCESS, companyFacade.updateCompanyBillingData(request)));
  }

  /**
   * Associates an uploaded media asset as the company logo.
   *
   * @param request media asset selected as the logo
   * @return confirmation response
   */
  @PostMapping(LOGO_ENDPOINT)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> updateLogo(@RequestBody LogoUpdateRequest request) {
    companyFacade.updateLogo(request.getMediaAssetId());

    return ResponseEntity.ok(ApiResponse.success(CompanyMessageKey.LOGO_UPDATED, null));
  }

  /**
   * Changes the company preferred currency.
   *
   * @param request currency selected for the company
   * @return response containing the updated currency
   */
  @PostMapping(PREFERRED_CURRENCY_ENDPOINT)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UpdatedPreferredCurrencyResponse>> updatePreferredCurrency(
      @RequestBody CompanyPreferredCurrencyUpdateRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            CompanyMessageKey.CURRENCY_UPDATED, companyFacade.updatePreferredCurrency(request)));
  }
}
