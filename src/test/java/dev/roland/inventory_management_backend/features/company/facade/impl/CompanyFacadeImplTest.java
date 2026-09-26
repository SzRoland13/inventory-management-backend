package dev.roland.inventory_management_backend.features.company.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.dto.CompanyBaseDataUpdateRequest;
import dev.roland.inventory_management_backend.features.company.dto.CompanyBillingDataUpdateRequest;
import dev.roland.inventory_management_backend.features.company.dto.CompanyPreferredCurrencyUpdateRequest;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.currency.service.CurrencyService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaPreviewResponse;
import dev.roland.inventory_management_backend.features.media_asset.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;

@ExtendWith(MockitoExtension.class)
class CompanyFacadeImplTest {

  @Mock private CompanyService companyService;
  @Mock private MediaUsageService mediaUsageService;
  @Mock private MediaAssetFacade mediaAssetFacade;
  @Mock private MediaAssetService mediaAssetService;
  @Mock private ObjectStorageService objectStorageService;
  @Mock private CurrencyService currencyService;

  private CompanyFacadeImpl facade;

  @BeforeEach
  void setUp() {
    facade =
        new CompanyFacadeImpl(
            companyService,
            mediaUsageService,
            mediaAssetFacade,
            mediaAssetService,
            objectStorageService,
            currencyService);
  }

  @Test
  void updatesCompanyBaseAndBillingData() {
    final Company company = Company.builder().id(5L).name("Old").build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(companyService.save(company)).thenReturn(company);

    final var base =
        facade.updateCompanyBaseData(
            CompanyBaseDataUpdateRequest.builder()
                .name("Acme")
                .description("Supplies")
                .email("office@example.com")
                .phone("555")
                .address("Main street")
                .website("https://example.com")
                .build());
    assertEquals("Acme", base.getName());
    assertEquals("Supplies", company.getDescription());
    assertEquals("office@example.com", company.getEmail());

    final var billing =
        facade.updateCompanyBillingData(
            CompanyBillingDataUpdateRequest.builder()
                .taxNumber("T1")
                .vatNumber("V1")
                .registrationNumber("R1")
                .bankAccount("B1")
                .iban("I1")
                .build());
    assertEquals("T1", billing.getTaxNumber());
    assertEquals("I1", company.getIban());
  }

  @Test
  void updatesPreferredCurrencyForTheRequestedCompany() {
    final Company company = Company.builder().id(2L).build();
    final Currency currency = Currency.builder().id(3L).code("EUR").build();
    when(companyService.findByIdOrThrow(2L)).thenReturn(company);
    when(currencyService.findByIdOrThrow(3L)).thenReturn(currency);
    when(companyService.save(company)).thenReturn(company);
    final CompanyPreferredCurrencyUpdateRequest request =
        new CompanyPreferredCurrencyUpdateRequest();
    request.setCompanyId(2L);
    request.setCurrencyId(3L);

    final var result = facade.updatePreferredCurrency(request);

    assertEquals(2L, result.getCompanyId());
    assertEquals(currency, company.getPreferredCurrency());
    verify(companyService).save(company);
  }

  @Test
  void nullOrUnchangedLogoDoesNotMoveOrDeleteMedia() {
    final Company company = Company.builder().id(12L).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);

    facade.updateLogo(null);
    when(mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.COMPANY, 12L, MediaUsageType.LOGO))
        .thenReturn(
            Optional.of(
                MediaUsage.builder().mediaAsset(MediaAsset.builder().id(44L).build()).build()));

    facade.updateLogo(44L);

    verify(objectStorageService, never()).move(any(), any());
    verify(mediaAssetService, never()).findByIdOrThrow(any());
  }

  @Test
  void replacingLogoDeletesUnusedOldAssetAndMovesNewAssetToPermanentPath() {
    final Company company = Company.builder().id(12L).build();
    final MediaAsset oldAsset = MediaAsset.builder().id(44L).build();
    final MediaAsset newAsset =
        MediaAsset.builder().id(45L).filename("logo.png").objectPath("temp/logo.png").build();
    final MediaUsage oldUsage = MediaUsage.builder().mediaAsset(oldAsset).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.COMPANY, 12L, MediaUsageType.LOGO))
        .thenReturn(Optional.of(oldUsage));
    when(mediaUsageService.usageCountByMediaAssetId(44L)).thenReturn(0L);
    when(mediaAssetService.findByIdOrThrow(45L)).thenReturn(newAsset);
    when(objectStorageService.generateSolidObjectPath(
            "logo.png", MediaEntityType.COMPANY, 12L, MediaUsageType.LOGO))
        .thenReturn("company/12/logo/logo.png");

    facade.updateLogo(45L);

    verify(mediaUsageService).delete(oldUsage);
    verify(mediaAssetFacade).deleteAsset(44L);
    verify(objectStorageService).move("temp/logo.png", "company/12/logo/logo.png");
    assertEquals("company/12/logo/logo.png", newAsset.getObjectPath());
    verify(mediaAssetService).save(newAsset);
    verify(mediaUsageService).save(any(MediaUsage.class));
  }

  @Test
  void minimalAndExtendedCompanyResponsesIncludeLogoAndPreferredCurrencyWhenPresent() {
    final Currency currency =
        Currency.builder().id(2L).code("USD").name("Dollar").symbol("$").build();
    final Company company =
        Company.builder().id(12L).name("Acme").preferredCurrency(currency).build();
    final MediaAsset logo = MediaAsset.builder().id(30L).build();
    final MediaUsage usage = MediaUsage.builder().mediaAsset(logo).build();
    final Instant expiry = Instant.parse("2030-01-01T00:00:00Z");
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.COMPANY, 12L, MediaUsageType.LOGO))
        .thenReturn(Optional.of(usage));
    when(mediaAssetFacade.getPreview(30L))
        .thenReturn(new MediaPreviewResponse(30L, "https://logo", expiry));

    final var minimal = facade.getMinimalCompanyData();
    final var extended = facade.getExtendedCompanyData();

    assertEquals(30L, minimal.getLogoId());
    assertEquals("https://logo", minimal.getLogoUrl());
    assertEquals(expiry, minimal.getLogoUrlExpiry());
    assertEquals("Acme", extended.getName());
    assertEquals("USD", extended.getPreferredCurrency().getCode());
  }

  @Test
  void newLogoWithNoExistingUsageIsStoredWithoutDeletingAnOldAsset() {
    final Company company = Company.builder().id(12L).build();
    final MediaAsset newAsset =
        MediaAsset.builder().id(45L).filename("logo.png").objectPath("temp/logo.png").build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.COMPANY, 12L, MediaUsageType.LOGO))
        .thenReturn(Optional.empty());
    when(mediaAssetService.findByIdOrThrow(45L)).thenReturn(newAsset);
    when(objectStorageService.generateSolidObjectPath(
            "logo.png", MediaEntityType.COMPANY, 12L, MediaUsageType.LOGO))
        .thenReturn("company/12/logo/logo.png");

    facade.updateLogo(45L);

    verify(mediaUsageService, never()).delete(any());
    verify(mediaAssetFacade, never()).deleteAsset(any());
    verify(mediaUsageService).save(any());
  }
}
