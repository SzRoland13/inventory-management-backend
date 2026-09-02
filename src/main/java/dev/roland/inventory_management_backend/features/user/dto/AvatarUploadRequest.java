package dev.roland.inventory_management_backend.features.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvatarUploadRequest {
  private Long mediaAssetId;
}
