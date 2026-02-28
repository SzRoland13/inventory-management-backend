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
    Optional<Company> company = getCompany();

    return company.map(this::buildBaseResponse).orElseGet(this::buildEmptyBaseResponse);
  }

  @Override
  public CompanyExtendedResponse getExtendedCompanyData() {
    Optional<Company> company = getCompany();

    return company.map(this::buildExtendedResponse).orElseGet(this::buildEmptyExtendedResponse);
  }

  @Override
  @Transactional
  public CompanyExtendedResponse updateCompany(CompanyUpdateRequest request) {

    Company company = getCompanyOrCreateNew();

    company.setName(request.getName());
    company.setDescription(request.getDescription());
    company.setEmail(request.getEmail());
    company.setPhone(request.getPhone());
    company.setAddress(request.getAddress());
    company.setWebsite(request.getWebsite());

    company = companyService.save(company);

    handleLogoUpdate(company, request.getLogoMediaAssetId());

    return buildExtendedResponse(company);
  }

  private Optional<Company> getCompany() {
    return companyService.findAll().stream().findFirst();
  }

  private Company getCompanyOrCreateNew() {
    return getCompany().orElseGet(() -> companyService.save(Company.builder().build()));
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

    String url = null;
    Instant expiry = null;

    if (logoUsage.isPresent()) {
      MediaPreviewResponse presigned =
          mediaAssetFacade.getPreview(logoUsage.get().getMediaAsset().getId());

      url = presigned.getGetUrl();
      expiry = presigned.getExpiry();
    }

    return new CompanyBaseDataResponse(company.getName(), url, expiry, true);
  }

  private CompanyExtendedResponse buildExtendedResponse(Company company) {

    CompanyBaseDataResponse base = buildBaseResponse(company);

    return new CompanyExtendedResponse(
        company.getName(),
        base.getLogoUrl(),
        base.getLogoUrlExpiry(),
        company.getDescription(),
        company.getEmail(),
        company.getPhone(),
        company.getAddress(),
        company.getWebsite(),
        true);
  }

  private Optional<MediaUsage> findLogoUsage(Long companyId) {
    return mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
        MediaEntityType.COMPANY, companyId, MediaUsageType.LOGO);
  }
}
