package dev.roland.inventory_management_backend.dto.media;

import dev.roland.inventory_management_backend.enums.MediaEntityType;
import dev.roland.inventory_management_backend.enums.MediaUsageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaUploadInitRequest {
  private String filename;
  private String mimeType;
  private MediaEntityType mediaEntityType;
  private MediaUsageType mediaUsageType;
}
