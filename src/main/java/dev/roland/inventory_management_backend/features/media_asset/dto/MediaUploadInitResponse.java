package dev.roland.inventory_management_backend.dto.media;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaUploadInitResponse {
  private Long id;
  private String putUrl;
  private Instant expiry;
}
