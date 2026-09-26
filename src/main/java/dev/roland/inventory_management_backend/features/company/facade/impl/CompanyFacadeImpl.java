package dev.roland.inventory_management_backend.features.company.facade.impl;

import java.time.Instant;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.dto.CompanyBaseDataResponse;
import dev.roland.inventory_management_backend.features.company.dto.CompanyBaseDataUpdateRequest;
import dev.roland.inventory_management_backend.features.company.dto.CompanyBillingDataResponse;
import dev.roland.inventory_management_backend.features.company.dto.CompanyBillingDataUpdateRequest;
import dev.roland.inventory_management_backend.features.company.dto.CompanyExtendedResponse;
import dev.roland.inventory_management_backend.features.company.dto.CompanyMinimalResponse;
import dev.roland.inventory_management_backend.features.company.dto.CompanyPreferredCurrencyUpdateRequest;
import dev.roland.inventory_management_backend.features.company.dto.UpdatedPreferredCurrencyResponse;
import dev.roland.inventory_management_backend.features.company.facade.CompanyFacade;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.currency.dto.CurrencyResponse;
import dev.roland.inventory_management_backend.features.currency.service.CurrencyService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.dto.MediaPreviewResponse;
import dev.roland.inventory_management_backend.features.media_asset.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import lombok.RequiredArgsConstructor;

/** Coordinates company profile updates and associated media changes. */
@Service
@RequiredArgsConstructor
public class CompanyFacadeImpl implements CompanyFacade {
  private final CompanyService companyService;
  private final MediaUsageService mediaUsageService;
  private final MediaAssetFacade mediaAssetFacade;
  private final MediaAssetService mediaAssetService;
  private final ObjectStorageService objectStorageService;
  private final CurrencyService currencyService;

  /** {@inheritDoc} */
  @Override
  public CompanyMinimalResponse getMinimalCompanyData() {
    final Company company = companyService.getCompanyOrCreateNew();

    return buildMinimalResponse(company);
  }

  /** {@inheritDoc} */
  @Override
  public CompanyExtendedResponse getExtendedCompanyData() {
    final Company company = companyService.getCompanyOrCreateNew();

    return buildExtendedResponse(company);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CompanyBaseDataResponse updateCompanyBaseData(final CompanyBaseDataUpdateRequest request) {

    Company company = companyService.getCompanyOrCreateNew();

    company.setName(request.getName());
    company.setDescription(request.getDescription());
    company.setEmail(request.getEmail());
    company.setPhone(request.getPhone());
    company.setAddress(request.getAddress());
    company.setWebsite(request.getWebsite());

    company = companyService.save(company);

    return buildBaseDataResponse(company);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void updateLogo(final Long mediaAssetId) {
    final Company company = companyService.getCompanyOrCreateNew();

    handleLogoUpdate(company, mediaAssetId);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CompanyBillingDataResponse updateCompanyBillingData(
      final CompanyBillingDataUpdateRequest request) {
    final Company company = companyService.getCompanyOrCreateNew();

    company.setTaxNumber(request.getTaxNumber());
    company.setVatNumber(request.getVatNumber());
    company.setRegistrationNumber(request.getRegistrationNumber());
    company.setBankAccount(request.getBankAccount());
    company.setIban(request.getIban());

    return buildCompanyBillingDataResponse(company);
  }

  /**
   * {@inheritDoc}
   *
   * @param request request supplied to this method
   * @return update preferred currency result
   */
  @Override
  @Transactional
  public UpdatedPreferredCurrencyResponse updatePreferredCurrency(
      final CompanyPreferredCurrencyUpdateRequest request) {
    final Company company = companyService.findByIdOrThrow(request.getCompanyId());
    final Currency currency = currencyService.findByIdOrThrow(request.getCurrencyId());

    company.setPreferredCurrency(currency);
    companyService.save(company);

    return UpdatedPreferredCurrencyResponse.builder()
        .companyId(company.getId())
        .currency(currency)
        .build();
  }

  private void handleLogoUpdate(final Company company, final Long newMediaId) {
    if (newMediaId == null) {
      return;
    }

    final Optional<MediaUsage> existingUsage =
        mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.COMPANY, company.getId(), MediaUsageType.LOGO);

    if (existingUsage.isEmpty()
        || !existingUsage.get().getMediaAsset().getId().equals(newMediaId)) {
      if (existingUsage.isPresent()) {
        final MediaUsage usage = existingUsage.get();
        final MediaAsset oldAsset = usage.getMediaAsset();

        mediaUsageService.delete(usage);

        if (mediaUsageService.usageCountByMediaAssetId(oldAsset.getId()) == 0) {
          mediaAssetFacade.deleteAsset(oldAsset.getId());
        }
      }

      final MediaAsset newAsset = mediaAssetService.findByIdOrThrow(newMediaId);

      final String newPath =
          objectStorageService.generateSolidObjectPath(
              newAsset.getFilename(),
              MediaEntityType.COMPANY,
              company.getId(),
              MediaUsageType.LOGO);

      objectStorageService.move(newAsset.getObjectPath(), newPath);

      newAsset.setObjectPath(newPath);
      mediaAssetService.save(newAsset);

      final MediaUsage usage =
          MediaUsage.builder()
              .mediaAsset(newAsset)
              .entityType(MediaEntityType.COMPANY)
              .entityId(company.getId())
              .usageType(MediaUsageType.LOGO)
              .build();

      mediaUsageService.save(usage);
    }
  }

  private CompanyBaseDataResponse buildBaseDataResponse(final Company company) {
    return CompanyBaseDataResponse.builder()
        .id(company.getId())
        .name(company.getName())
        .description(company.getDescription())
        .email(company.getEmail())
        .phone(company.getPhone())
        .address(company.getAddress())
        .website(company.getWebsite())
        .build();
  }

  private CompanyBillingDataResponse buildCompanyBillingDataResponse(final Company company) {
    return CompanyBillingDataResponse.builder()
        .id(company.getId())
        .taxNumber(company.getTaxNumber())
        .vatNumber(company.getVatNumber())
        .registrationNumber(company.getRegistrationNumber())
        .bankAccount(company.getBankAccount())
        .iban(company.getIban())
        .build();
  }

  private CompanyMinimalResponse buildMinimalResponse(final Company company) {

    final Optional<MediaUsage> logoUsage = findLogoUsage(company.getId());

    Long id = null;
    String url = null;
    Instant expiry = null;

    if (logoUsage.isPresent()) {
      final MediaPreviewResponse presigned =
          mediaAssetFacade.getPreview(logoUsage.get().getMediaAsset().getId());

      id = presigned.getId();
      url = presigned.getGetUrl();
      expiry = presigned.getExpiry();
    }

    return new CompanyMinimalResponse(company.getId(), company.getName(), id, url, expiry);
  }

  private CompanyExtendedResponse buildExtendedResponse(final Company company) {

    final CompanyMinimalResponse base = buildMinimalResponse(company);

    return new CompanyExtendedResponse(
        company.getId(),
        company.getName(),
        base.getLogoId(),
        base.getLogoUrl(),
        base.getLogoUrlExpiry(),
        company.getDescription(),
        company.getEmail(),
        company.getPhone(),
        company.getAddress(),
        company.getWebsite(),
        company.getTaxNumber(),
        company.getVatNumber(),
        company.getRegistrationNumber(),
        company.getBankAccount(),
        company.getIban(),
        CurrencyResponse.toDto(company.getPreferredCurrency()));
  }

  private Optional<MediaUsage> findLogoUsage(final Long companyId) {
    return mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
        MediaEntityType.COMPANY, companyId, MediaUsageType.LOGO);
  }
}
