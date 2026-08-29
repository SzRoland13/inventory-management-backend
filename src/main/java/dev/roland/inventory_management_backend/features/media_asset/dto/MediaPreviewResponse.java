package dev.roland.inventory_management_backend.dto.media;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaPreviewResponse {
  private Long id;
  private String getUrl;
  private Instant expiry;
}
