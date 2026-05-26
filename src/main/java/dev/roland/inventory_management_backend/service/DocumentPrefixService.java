package dev.roland.inventory_management_backend.service;

import java.util.List;

import dev.roland.inventory_management_backend.dto.document_prefix.DocumentPrefixDto;
import dev.roland.inventory_management_backend.dto.document_prefix.DocumentPrefixesResponse;
import dev.roland.inventory_management_backend.model.DocumentPrefix;

public interface DocumentPrefixService extends BaseService<DocumentPrefix, Long> {
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
