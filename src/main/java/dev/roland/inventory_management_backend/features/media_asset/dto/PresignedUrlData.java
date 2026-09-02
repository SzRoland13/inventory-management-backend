package dev.roland.inventory_management_backend.features.media_asset.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PresignedUrlData {
  private String url;
  private Instant expiry;
}
