package dev.roland.inventory_management_backend.features.document_prefix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import lombok.Builder;
import lombok.Data;

/** Shapes document numbering prefix data returned to API clients. */
@Data
@Builder
public class DocumentPrefixDto {
  private Long id;

  @NotNull(message = ValidationKeys.REQUIRED)
  private DocumentType documentType;

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Size(max = 20, message = ValidationKeys.TOO_LONG)
  private String prefix;

  /**
   * Builds a prefix response from the supplied prefix values.
   *
   * @param prefix prefix supplied to this method
   * @return to dto result
   */
  public static DocumentPrefixDto toDto(final DocumentPrefix prefix) {
    return DocumentPrefixDto.builder()
        .id(prefix.getId())
        .documentType(prefix.getDocumentType())
        .prefix(prefix.getPrefix())
        .build();
  }
}
