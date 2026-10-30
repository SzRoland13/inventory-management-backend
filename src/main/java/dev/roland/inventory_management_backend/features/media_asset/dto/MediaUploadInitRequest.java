package dev.roland.inventory_management_backend.features.media_asset.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Carries input data for media Upload Init in the media asset API. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaUploadInitRequest {
  @NotBlank private String filename;

  @NotBlank
  @Pattern(regexp = "image/(jpeg|png|webp)")
  private String mimeType;

  @NotNull
  @Positive
  @Max(5_242_880)
  private Long fileSize;
}
