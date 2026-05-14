package dev.roland.inventory_management_backend.dto.document_prefix;

import dev.roland.inventory_management_backend.enums.DocumentType;
import dev.roland.inventory_management_backend.model.DocumentPrefix;
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
