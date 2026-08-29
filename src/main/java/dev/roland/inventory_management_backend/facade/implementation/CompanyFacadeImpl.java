package dev.roland.inventory_management_backend.facade.implementation;

import java.time.Instant;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataUpdateRequest;
import dev.roland.inventory_management_backend.dto.company.CompanyBillingDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyBillingDataUpdateRequest;
import dev.roland.inventory_management_backend.dto.company.CompanyExtendedResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyMinimalResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyPreferredCurrencyUpdateRequest;
import dev.roland.inventory_management_backend.dto.company.UpdatedPreferredCurrencyResponse;
import dev.roland.inventory_management_backend.dto.currency.CurrencyResponse;
import dev.roland.inventory_management_backend.dto.media.MediaPreviewResponse;
import dev.roland.inventory_management_backend.enums.MediaEntityType;
import dev.roland.inventory_management_backend.enums.MediaUsageType;
import dev.roland.inventory_management_backend.facade.CompanyFacade;
import dev.roland.inventory_management_backend.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.model.Company;
import dev.roland.inventory_management_backend.model.Currency;
import dev.roland.inventory_management_backend.model.MediaAsset;
import dev.roland.inventory_management_backend.model.MediaUsage;
import dev.roland.inventory_management_backend.service.CompanyService;
import dev.roland.inventory_management_backend.service.CurrencyService;
import dev.roland.inventory_management_backend.service.MediaAssetService;
import dev.roland.inventory_management_backend.service.MediaUsageService;
import dev.roland.inventory_management_backend.service.common.ObjectStorageService;
import lombok.RequiredArgsConstructor;

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
    Company company = companyService.getCompanyOrCreateNew();

    return buildMinimalResponse(company);
  }

  /** {@inheritDoc} */
  @Override
  public CompanyExtendedResponse getExtendedCompanyData() {
    Company company = companyService.getCompanyOrCreateNew();

    return buildExtendedResponse(company);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CompanyBaseDataResponse updateCompanyBaseData(CompanyBaseDataUpdateRequest request) {

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
  public void updateLogo(Long mediaAssetId) {
    Company company = companyService.getCompanyOrCreateNew();

    handleLogoUpdate(company, mediaAssetId);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public CompanyBillingDataResponse updateCompanyBillingData(
      CompanyBillingDataUpdateRequest request) {
    Company company = companyService.getCompanyOrCreateNew();

    company.setTaxNumber(request.getTaxNumber());
    company.setVatNumber(request.getVatNumber());
    company.setRegistrationNumber(request.getRegistrationNumber());
    company.setBankAccount(request.getBankAccount());
    company.setIban(request.getIban());

    return buildCompanyBillingDataResponse(company);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public UpdatedPreferredCurrencyResponse updatePreferredCurrency(
      CompanyPreferredCurrencyUpdateRequest request) {
    Company company = companyService.findByIdOrThrow(request.getCompanyId());
    Currency currency = currencyService.findByIdOrThrow(request.getCurrencyId());

    company.setPreferredCurrency(currency);
    companyService.save(company);

    return UpdatedPreferredCurrencyResponse.builder()
        .companyId(company.getId())
        .currency(currency)
        .build();
  }

  private void handleLogoUpdate(Company company, Long newMediaId) {
    if (newMediaId == null) return;

    Optional<MediaUsage> existingUsage =
        mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.COMPANY, company.getId(), MediaUsageType.LOGO);

    if (existingUsage.isPresent()
        && existingUsage.get().getMediaAsset().getId().equals(newMediaId)) {
      return;
    }

    if (existingUsage.isPresent()) {
      MediaUsage usage = existingUsage.get();
      MediaAsset oldAsset = usage.getMediaAsset();

      mediaUsageService.delete(usage);

      if (mediaUsageService.usageCountByMediaAssetId(oldAsset.getId()) == 0) {
        mediaAssetFacade.deleteAsset(oldAsset.getId());
      }
    }

    MediaAsset newAsset = mediaAssetService.findByIdOrThrow(newMediaId);

    String newPath =
        objectStorageService.generateSolidObjectPath(
            newAsset.getFilename(), MediaEntityType.COMPANY, company.getId(), MediaUsageType.LOGO);

    objectStorageService.move(newAsset.getObjectPath(), newPath);

    newAsset.setObjectPath(newPath);
    mediaAssetService.save(newAsset);

    MediaUsage usage =
        MediaUsage.builder()
            .mediaAsset(newAsset)
            .entityType(MediaEntityType.COMPANY)
            .entityId(company.getId())
            .usageType(MediaUsageType.LOGO)
            .build();

    mediaUsageService.save(usage);
  }

  private CompanyBaseDataResponse buildBaseDataResponse(Company company) {
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

  private CompanyBillingDataResponse buildCompanyBillingDataResponse(Company company) {
    return CompanyBillingDataResponse.builder()
        .id(company.getId())
        .taxNumber(company.getTaxNumber())
        .vatNumber(company.getVatNumber())
        .registrationNumber(company.getRegistrationNumber())
        .bankAccount(company.getBankAccount())
        .iban(company.getIban())
        .build();
  }

  private CompanyMinimalResponse buildMinimalResponse(Company company) {

    Optional<MediaUsage> logoUsage = findLogoUsage(company.getId());

    Long id = null;
    String url = null;
    Instant expiry = null;

    if (logoUsage.isPresent()) {
      MediaPreviewResponse presigned =
          mediaAssetFacade.getPreview(logoUsage.get().getMediaAsset().getId());

      id = presigned.getId();
      url = presigned.getGetUrl();
      expiry = presigned.getExpiry();
    }

    return new CompanyMinimalResponse(company.getId(), company.getName(), id, url, expiry);
  }

  private CompanyExtendedResponse buildExtendedResponse(Company company) {

    CompanyMinimalResponse base = buildMinimalResponse(company);

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

  private Optional<MediaUsage> findLogoUsage(Long companyId) {
    return mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
        MediaEntityType.COMPANY, companyId, MediaUsageType.LOGO);
  }
}
