package dev.roland.inventory_management_backend.features.company.dto;

import lombok.Builder;
import lombok.Data;

/** Carries input data for logo Update in the company profile API. */
@Data
@Builder
public class LogoUpdateRequest {
  private Long mediaAssetId;
}
