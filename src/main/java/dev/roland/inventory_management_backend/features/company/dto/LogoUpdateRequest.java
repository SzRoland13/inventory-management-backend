package dev.roland.inventory_management_backend.features.company.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogoUpdateRequest {
  private Long mediaAssetId;
}
