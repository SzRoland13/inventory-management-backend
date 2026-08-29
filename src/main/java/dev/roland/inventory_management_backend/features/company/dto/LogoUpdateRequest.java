package dev.roland.inventory_management_backend.dto.company;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogoUpdateRequest {
  private Long mediaAssetId;
}
