package dev.roland.inventory_management_backend.features.document_prefix.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixDto;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixesResponse;

/** Defines operations supported by the document numbering prefix feature. */
public interface DocumentPrefixService extends BaseService<DocumentPrefix, Long> {
  /**
   * Finds the first configured prefix for a company and document type.
   *
   * @param companyId owning company identifier
   * @param type document type
   * @return matching prefix, if found
   */
  Optional<DocumentPrefix> findFirstByCompanyIdAndDocumentTypeOrderByIdAsc(
      Long companyId, DocumentType type);

  /**
   * Returns configured document prefixes, or default prefixes when none have been saved yet.
   *
   * @return document prefix response
   */
  DocumentPrefixesResponse getAllPrefixes();

  /**
   * Creates or updates document prefixes from the supplied DTOs.
   *
   * @param prefixes prefix values to persist
   * @return refreshed document prefix response
   */
  DocumentPrefixesResponse updatePrefixes(List<DocumentPrefixDto> prefixes);
}
