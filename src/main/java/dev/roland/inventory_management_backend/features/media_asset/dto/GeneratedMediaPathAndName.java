package dev.roland.inventory_management_backend.dto.media;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeneratedMediaPathAndName {
  private String objectPath;
  private String filename;
}
