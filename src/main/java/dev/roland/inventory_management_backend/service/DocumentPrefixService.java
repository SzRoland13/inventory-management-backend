package dev.roland.inventory_management_backend.service;

import java.util.List;

import dev.roland.inventory_management_backend.dto.document_prefix.DocumentPrefixDto;
import dev.roland.inventory_management_backend.dto.document_prefix.DocumentPrefixesResponse;
import dev.roland.inventory_management_backend.model.DocumentPrefix;

public interface DocumentPrefixService extends BaseService<DocumentPrefix, Long> {
  DocumentPrefixesResponse getAllPrefixes();

  DocumentPrefixesResponse updatePrefixes(List<DocumentPrefixDto> prefixes);
}
