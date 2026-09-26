package dev.roland.inventory_management_backend.features.media_asset.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Carries the result of presigned Url Data in the media asset workflow. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PresignedUrlData {
  private String url;
  private Instant expiry;
}
