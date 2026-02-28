package dev.roland.inventory_management_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyExtendedResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyUpdateRequest;
import dev.roland.inventory_management_backend.facade.CompanyFacade;
import dev.roland.inventory_management_backend.messageKey.GenericMessageKey;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(CompanyController.COMPANY_BASE_ENDPOINT)
@RequiredArgsConstructor
public class CompanyController {
  public static final String COMPANY_BASE_ENDPOINT = "api/v1/company";
  public static final String COMPANY_EXTENDED_ENDPOINT = "/extended";

  private final CompanyFacade companyFacade;

  @GetMapping
  public ResponseEntity<ApiResponse<CompanyBaseDataResponse>> getBaseCompanyData() {
    return ResponseEntity.ok(
        ApiResponse.success(GenericMessageKey.REQUEST_SUCCESS, companyFacade.getBaseCompanyData()));
  }

  @GetMapping(COMPANY_EXTENDED_ENDPOINT)
  public ResponseEntity<ApiResponse<CompanyExtendedResponse>> getExtendedCompanyData() {
    return ResponseEntity.ok(
        ApiResponse.success(
            GenericMessageKey.REQUEST_SUCCESS, companyFacade.getExtendedCompanyData()));
  }

  @PutMapping
  public ResponseEntity<ApiResponse<CompanyExtendedResponse>> updateCompany(
      @RequestBody CompanyUpdateRequest request) {

    return ResponseEntity.ok(
        ApiResponse.success(
            GenericMessageKey.REQUEST_SUCCESS, companyFacade.updateCompany(request)));
  }
}
