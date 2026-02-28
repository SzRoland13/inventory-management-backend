package dev.roland.inventory_management_backend.dto.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaUploadInitRequest {
  private String filename;
  private String mimeType;
}
