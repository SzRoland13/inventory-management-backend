package dev.roland.inventory_management_backend.facade.implementation;

import java.time.Instant;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.dto.company.CompanyBaseDataResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyExtendedResponse;
import dev.roland.inventory_management_backend.dto.company.CompanyUpdateRequest;
import dev.roland.inventory_management_backend.dto.media.MediaPreviewResponse;
import dev.roland.inventory_management_backend.enums.MediaEntityType;
import dev.roland.inventory_management_backend.enums.MediaUsageType;
import dev.roland.inventory_management_backend.facade.CompanyFacade;
import dev.roland.inventory_management_backend.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.model.Company;
import dev.roland.inventory_management_backend.model.MediaAsset;
import dev.roland.inventory_management_backend.model.MediaUsage;
import dev.roland.inventory_management_backend.service.CompanyService;
import dev.roland.inventory_management_backend.service.MediaAssetService;
import dev.roland.inventory_management_backend.service.MediaUsageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyFacadeImpl implements CompanyFacade {
  private final CompanyService companyService;
  private final MediaUsageService mediaUsageService;
  private final MediaAssetFacade mediaAssetFacade;
  private final MediaAssetService mediaAssetService;

  @Override
  public CompanyBaseDataResponse getBaseCompanyData() {
    Optional<Company> company = companyService.findFirstByOrderByIdAsc();

    return company.map(this::buildBaseResponse).orElseGet(this::buildEmptyBaseResponse);
  }

  @Override
  public CompanyExtendedResponse getExtendedCompanyData() {
    Optional<Company> company = companyService.findFirstByOrderByIdAsc();

    return company.map(this::buildExtendedResponse).orElseGet(this::buildEmptyExtendedResponse);
  }

  @Override
  @Transactional
  public CompanyExtendedResponse updateCompany(CompanyUpdateRequest request) {

    Company company = companyService.getCompanyOrCreateNew();

    company.setName(request.getName());
    company.setDescription(request.getDescription());
    company.setEmail(request.getEmail());
    company.setPhone(request.getPhone());
    company.setAddress(request.getAddress());
    company.setWebsite(request.getWebsite());
    company.setTaxNumber(request.getTaxNumber());
    company.setVatNumber(request.getVatNumber());
    company.setRegistrationNumber(request.getRegistrationNumber());
    company.setBankAccount(request.getBankAccount());
    company.setIban(request.getIban());

    company = companyService.save(company);

    return buildExtendedResponse(company);
  }

  @Override
  public void updateLogo(Long mediaAssetId) {
    Company company = companyService.getCompanyOrCreateNew();

    handleLogoUpdate(company, mediaAssetId);
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

    MediaUsage usage =
        MediaUsage.builder()
            .mediaAsset(newAsset)
            .entityType(MediaEntityType.COMPANY)
            .entityId(company.getId())
            .usageType(MediaUsageType.LOGO)
            .build();

    mediaUsageService.save(usage);
  }

  private CompanyBaseDataResponse buildEmptyBaseResponse() {
    return CompanyBaseDataResponse.builder().exists(false).build();
  }

  private CompanyExtendedResponse buildEmptyExtendedResponse() {
    return CompanyExtendedResponse.builder().exists(false).build();
  }

  private CompanyBaseDataResponse buildBaseResponse(Company company) {

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

    return new CompanyBaseDataResponse(company.getId(), company.getName(), id, url, expiry, true);
  }

  private CompanyExtendedResponse buildExtendedResponse(Company company) {

    CompanyBaseDataResponse base = buildBaseResponse(company);

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
        true);
  }

  private Optional<MediaUsage> findLogoUsage(Long companyId) {
    return mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
        MediaEntityType.COMPANY, companyId, MediaUsageType.LOGO);
  }
}
