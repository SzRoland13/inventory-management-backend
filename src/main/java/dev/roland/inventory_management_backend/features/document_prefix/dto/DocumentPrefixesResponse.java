package dev.roland.inventory_management_backend.features.document_prefix.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentPrefixesResponse {
  private List<DocumentPrefixDto> prefixes;
}
