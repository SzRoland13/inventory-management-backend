package dev.roland.inventory_management_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataUpdateRequest;
import dev.roland.inventory_management_backend.dto.company.CompanyBillingDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyBillingDataUpdateRequest;
import dev.roland.inventory_management_backend.dto.company.CompanyExtendedResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyMinimalResponse;
import dev.roland.inventory_management_backend.dto.company.LogoUpdateRequest;
import dev.roland.inventory_management_backend.facade.CompanyFacade;
import dev.roland.inventory_management_backend.messageKey.CompanyMessageKey;
import dev.roland.inventory_management_backend.messageKey.GenericMessageKey;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(CompanyController.COMPANY_BASE_ENDPOINT)
@RequiredArgsConstructor
public class CompanyController {
  public static final String COMPANY_BASE_ENDPOINT = "api/v1/company";
  public static final String COMPANY_EXTENDED_ENDPOINT = "/extended";
  public static final String LOGO_ENDPOINT = "/logo";
  public static final String BILLING_ENDPOINT = "/billing";

  private final CompanyFacade companyFacade;

  @GetMapping
  public ResponseEntity<ApiResponse<CompanyMinimalResponse>> getMinimalCompanyData() {
    return ResponseEntity.ok(
        ApiResponse.success(
            GenericMessageKey.REQUEST_SUCCESS, companyFacade.getMinimalCompanyData()));
  }

  @GetMapping(COMPANY_EXTENDED_ENDPOINT)
  public ResponseEntity<ApiResponse<CompanyExtendedResponse>> getExtendedCompanyData() {
    return ResponseEntity.ok(
        ApiResponse.success(
            GenericMessageKey.REQUEST_SUCCESS, companyFacade.getExtendedCompanyData()));
  }

  @PutMapping
  public ResponseEntity<ApiResponse<CompanyBaseDataResponse>> updateCompanyBaseData(
      @RequestBody CompanyBaseDataUpdateRequest request) {

    return ResponseEntity.ok(
        ApiResponse.success(
            GenericMessageKey.REQUEST_SUCCESS, companyFacade.updateCompanyBaseData(request)));
  }

  @PutMapping(BILLING_ENDPOINT)
  public ResponseEntity<ApiResponse<CompanyBillingDataResponse>> updateCompanyBillingData(
      @RequestBody CompanyBillingDataUpdateRequest request) {

    return ResponseEntity.ok(
        ApiResponse.success(
            GenericMessageKey.REQUEST_SUCCESS, companyFacade.updateCompanyBillingData(request)));
  }

  @PostMapping(LOGO_ENDPOINT)
  public ResponseEntity<ApiResponse<Void>> updateLogo(@RequestBody LogoUpdateRequest request) {
    companyFacade.updateLogo(request.getMediaAssetId());

    return ResponseEntity.ok(ApiResponse.success(CompanyMessageKey.LOGO_UPDATED, null));
  }
}
