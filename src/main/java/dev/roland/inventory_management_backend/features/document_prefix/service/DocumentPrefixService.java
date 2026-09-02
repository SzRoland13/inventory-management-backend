package dev.roland.inventory_management_backend.features.document_prefix.service;

import java.util.List;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixDto;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixesResponse;

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
