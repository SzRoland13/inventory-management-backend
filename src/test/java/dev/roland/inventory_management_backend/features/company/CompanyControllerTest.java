package dev.roland.inventory_management_backend.features.company;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

  @Mock private CompanyFacade companyFacade;

  private CompanyController controller;

  @BeforeEach
  void setUp() {
    controller = new CompanyController(companyFacade);
  }

  @Test
  void exposesCompanyReadAndUpdateOperationsThroughFacade() {
    final CompanyMinimalResponse minimal = new CompanyMinimalResponse(1L, "Acme", null, null, null);
    final CompanyExtendedResponse extended = new CompanyExtendedResponse();
    when(companyFacade.getMinimalCompanyData()).thenReturn(minimal);
    when(companyFacade.getExtendedCompanyData()).thenReturn(extended);
    when(companyFacade.updateCompanyBaseData(org.mockito.ArgumentMatchers.any()))
        .thenReturn(CompanyBaseDataResponse.builder().id(1L).name("Acme").build());
    when(companyFacade.updateCompanyBillingData(org.mockito.ArgumentMatchers.any()))
        .thenReturn(CompanyBillingDataResponse.builder().id(1L).taxNumber("T1").build());
    when(companyFacade.updatePreferredCurrency(org.mockito.ArgumentMatchers.any()))
        .thenReturn(UpdatedPreferredCurrencyResponse.builder().companyId(1L).build());
    final CompanyBaseDataUpdateRequest baseRequest = new CompanyBaseDataUpdateRequest();
    final CompanyBillingDataUpdateRequest billingRequest = new CompanyBillingDataUpdateRequest();
    final CompanyPreferredCurrencyUpdateRequest currencyRequest =
        new CompanyPreferredCurrencyUpdateRequest();

    assertEquals(minimal, controller.getMinimalCompanyData().getBody().getPayload());
    assertEquals(extended, controller.getExtendedCompanyData().getBody().getPayload());
    assertEquals(200, controller.updateCompanyBaseData(baseRequest).getStatusCode().value());
    assertEquals(200, controller.updateCompanyBillingData(billingRequest).getStatusCode().value());
    assertEquals(200, controller.updatePreferredCurrency(currencyRequest).getStatusCode().value());

    verify(companyFacade).updateCompanyBaseData(baseRequest);
    verify(companyFacade).updateCompanyBillingData(billingRequest);
    verify(companyFacade).updatePreferredCurrency(currencyRequest);
  }

  @Test
  void logoEndpointAssociatesRequestedMediaAsset() {
    final LogoUpdateRequest request = LogoUpdateRequest.builder().mediaAssetId(22L).build();

    assertEquals(200, controller.updateLogo(request).getStatusCode().value());

    verify(companyFacade).updateLogo(22L);
  }
}
