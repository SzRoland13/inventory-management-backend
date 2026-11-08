package dev.roland.inventory_management_backend.features.media_asset.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Carries input data for media Upload Init in the media asset API. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaUploadInitRequest {
  @NotBlank(message = ValidationKeys.REQUIRED)
  @Size(max = 255, message = ValidationKeys.TOO_LONG)
  private String filename;

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Pattern(regexp = "image/(jpeg|png|webp)", message = ValidationKeys.INVALID_VALUE)
  private String mimeType;

  @NotNull(message = ValidationKeys.REQUIRED)
  @Positive(message = ValidationKeys.INVALID_VALUE)
  @Max(value = 5_242_880, message = ValidationKeys.INVALID_VALUE)
  private Long fileSize;
}
