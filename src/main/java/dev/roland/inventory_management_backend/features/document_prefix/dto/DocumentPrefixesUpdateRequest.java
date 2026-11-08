package dev.roland.inventory_management_backend.features.document_prefix.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import lombok.Builder;
import lombok.Data;

/** Carries input data for document Prefixes Update in the document numbering prefix API. */
@Data
@Builder
public class DocumentPrefixesUpdateRequest {
  @NotEmpty(message = ValidationKeys.REQUIRED)
  @Valid
  private List<DocumentPrefixDto> prefixes;
}
