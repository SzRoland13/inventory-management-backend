package dev.roland.inventory_management_backend.features.document_prefix.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/** Carries input data for document Prefixes Update in the document numbering prefix API. */
@Data
@Builder
public class DocumentPrefixesUpdateRequest {
  private List<DocumentPrefixDto> prefixes;
}
