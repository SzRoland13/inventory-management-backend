package dev.roland.inventory_management_backend.features.document_prefix.dto;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentPrefixDto {
  private Long id;
  private DocumentType documentType;
  private String prefix;

  public static DocumentPrefixDto toDto(DocumentPrefix prefix) {
    return DocumentPrefixDto.builder()
        .id(prefix.getId())
        .documentType(prefix.getDocumentType())
        .prefix(prefix.getPrefix())
        .build();
  }
}
