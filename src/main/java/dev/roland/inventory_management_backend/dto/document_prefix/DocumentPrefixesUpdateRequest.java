package dev.roland.inventory_management_backend.dto.document_prefix;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentPrefixesUpdateRequest {
  private List<DocumentPrefixDto> prefixes;
}
