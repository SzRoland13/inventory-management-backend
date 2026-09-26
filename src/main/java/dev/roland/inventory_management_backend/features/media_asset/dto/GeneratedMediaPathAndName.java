package dev.roland.inventory_management_backend.features.media_asset.dto;

import lombok.Builder;
import lombok.Data;

/** Carries the result of generated Media Path And Name in the media asset workflow. */
@Data
@Builder
public class GeneratedMediaPathAndName {
  private String objectPath;
  private String filename;
}
