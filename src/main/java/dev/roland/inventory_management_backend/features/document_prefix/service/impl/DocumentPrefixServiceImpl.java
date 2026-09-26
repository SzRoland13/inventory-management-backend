package dev.roland.inventory_management_backend.features.document_prefix.service.impl;

import java.util.Arrays;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixDto;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixesResponse;
import dev.roland.inventory_management_backend.features.document_prefix.repository.DocumentPrefixRepository;
import dev.roland.inventory_management_backend.features.document_prefix.service.DocumentPrefixService;
import lombok.RequiredArgsConstructor;

/** Implements the document numbering prefix service operations. */
@Service
@RequiredArgsConstructor
public class DocumentPrefixServiceImpl implements DocumentPrefixService {
  private final DocumentPrefixRepository documentPrefixRepository;
  private final CompanyService companyService;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<DocumentPrefix, Long> getRepository() {
    return documentPrefixRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.DOCUMENT_PREFIX;
  }

  /** {@inheritDoc} */
  @Override
  public DocumentPrefixesResponse getAllPrefixes() {
    final List<DocumentPrefix> prefixes = findAll();
    final List<DocumentPrefixDto> dtos;

    if (prefixes.isEmpty()) {
      dtos =
          Arrays.stream(DocumentType.values())
              .map(
                  dt ->
                      DocumentPrefixDto.builder()
                          .id(null)
                          .documentType(dt)
                          .prefix(dt.getDefaultPrefix())
                          .build())
              .toList();
    } else {
      dtos = prefixes.stream().map(DocumentPrefixDto::toDto).toList();
    }

    return DocumentPrefixesResponse.builder().prefixes(dtos).build();
  }

  /**
   * {@inheritDoc}
   *
   * @param dtos dtos supplied to this method
   * @return update prefixes result
   */
  @Transactional
  @Override
  public DocumentPrefixesResponse updatePrefixes(final List<DocumentPrefixDto> dtos) {
    final Company company = companyService.getCompanyOrCreateNew();

    for (DocumentPrefixDto dto : dtos) {
      final DocumentPrefix entity;
      if (dto.getId() != null) {
        entity = findByIdOrThrow(dto.getId());
      } else {
        entity =
            DocumentPrefix.builder().company(company).documentType(dto.getDocumentType()).build();
      }

      entity.setPrefix(dto.getPrefix());
      save(entity);
    }

    return getAllPrefixes();
  }
}
