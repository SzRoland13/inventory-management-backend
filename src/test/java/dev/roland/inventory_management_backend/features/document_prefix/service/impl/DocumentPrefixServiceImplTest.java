package dev.roland.inventory_management_backend.features.document_prefix.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixDto;
import dev.roland.inventory_management_backend.features.document_prefix.repository.DocumentPrefixRepository;

@ExtendWith(MockitoExtension.class)
class DocumentPrefixServiceImplTest {

  @Mock private DocumentPrefixRepository repository;
  @Mock private CompanyService companyService;

  private DocumentPrefixServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new DocumentPrefixServiceImpl(repository, companyService);
  }

  @Test
  void emptyPrefixTableReturnsDefaultsForEveryDocumentType() {
    when(repository.findAll()).thenReturn(List.of());

    final var response = service.getAllPrefixes();

    assertEquals(DocumentType.values().length, response.getPrefixes().size());
    assertEquals(
        "GR",
        response.getPrefixes().stream()
            .filter(dto -> dto.getDocumentType() == DocumentType.GOODS_RECEIPT)
            .findFirst()
            .orElseThrow()
            .getPrefix());
  }

  @Test
  void existingPrefixesAreMappedAndUpdatesHandleNewAndExistingRows() {
    final Company company = Company.builder().id(2L).build();
    final DocumentPrefix existing =
        DocumentPrefix.builder()
            .id(4L)
            .company(company)
            .documentType(DocumentType.GOODS_RECEIPT)
            .prefix("OLD")
            .build();
    when(repository.findAll()).thenReturn(List.of(existing));
    when(repository.findById(4L)).thenReturn(Optional.of(existing));
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(repository.save(any(DocumentPrefix.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final var mapped = service.getAllPrefixes();
    assertEquals("OLD", mapped.getPrefixes().get(0).getPrefix());

    final DocumentPrefixDto update =
        DocumentPrefixDto.builder()
            .id(4L)
            .documentType(DocumentType.GOODS_RECEIPT)
            .prefix("GR")
            .build();
    final DocumentPrefixDto create =
        DocumentPrefixDto.builder().documentType(DocumentType.DELIVERY_NOTE).prefix("SHIP").build();
    when(repository.findAll()).thenReturn(List.of(existing));
    final var result = service.updatePrefixes(List.of(update, create));

    assertEquals("GR", existing.getPrefix());
    assertEquals("GR", result.getPrefixes().get(0).getPrefix());
  }
}
