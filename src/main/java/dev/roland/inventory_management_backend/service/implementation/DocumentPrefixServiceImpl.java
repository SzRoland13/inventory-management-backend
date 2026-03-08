package dev.roland.inventory_management_backend.service.implementation;

import java.util.Arrays;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.dto.document_prefix.DocumentPrefixDto;
import dev.roland.inventory_management_backend.dto.document_prefix.DocumentPrefixesResponse;
import dev.roland.inventory_management_backend.enums.DocumentType;
import dev.roland.inventory_management_backend.messageKey.MessageKey;
import dev.roland.inventory_management_backend.messageKey.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.Company;
import dev.roland.inventory_management_backend.model.DocumentPrefix;
import dev.roland.inventory_management_backend.repository.DocumentPrefixRepository;
import dev.roland.inventory_management_backend.service.CompanyService;
import dev.roland.inventory_management_backend.service.DocumentPrefixService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentPrefixServiceImpl implements DocumentPrefixService {
  private final DocumentPrefixRepository documentPrefixRepository;
  private final CompanyService companyService;

  @Override
  public JpaRepository<DocumentPrefix, Long> getRepository() {
    return documentPrefixRepository;
  }

  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.DOCUMENT_PREFIX;
  }

  @Override
  public DocumentPrefixesResponse getAllPrefixes() {
    List<DocumentPrefix> prefixes = findAll();
    List<DocumentPrefixDto> dtos;

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

  @Transactional
  @Override
  public DocumentPrefixesResponse updatePrefixes(List<DocumentPrefixDto> dtos) {
    Company company = companyService.getCompanyOrCreateNew();

    for (DocumentPrefixDto dto : dtos) {
      DocumentPrefix entity;
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
