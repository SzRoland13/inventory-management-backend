package dev.roland.inventory_management_backend.features.user.dto;

import lombok.Builder;
import lombok.Data;

/** Carries the uploaded media asset identifier for a user-avatar update. */
@Data
@Builder
public class AvatarUploadRequest {
  private Long mediaAssetId;
}
