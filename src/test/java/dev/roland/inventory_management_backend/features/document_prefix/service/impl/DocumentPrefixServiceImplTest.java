package dev.roland.inventory_management_backend.features.document_prefix.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixDto;
import dev.roland.inventory_management_backend.features.document_prefix.repository.DocumentPrefixRepository;

@ExtendWith(MockitoExtension.class)
class DocumentPrefixServiceImplTest {

  @Mock private DocumentPrefixRepository repository;
  @Mock private CurrentCompanyProvider currentCompanyProvider;

  private DocumentPrefixServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new DocumentPrefixServiceImpl(repository, currentCompanyProvider);
    when(currentCompanyProvider.getCurrentCompany()).thenReturn(Company.builder().id(2L).build());
  }

  @Test
  void emptyPrefixTableReturnsDefaultsForEveryDocumentType() {
    when(repository.findAllByCompanyId(2L)).thenReturn(List.of());

    final var response = service.getAllPrefixes();

    assertEquals(DocumentType.values().length, response.getPrefixes().size());
    for (final DocumentType type : DocumentType.values()) {
      final var prefix =
          response.getPrefixes().stream()
              .filter(dto -> dto.getDocumentType() == type)
              .findFirst()
              .orElseThrow();
      assertEquals(type.getDefaultPrefix(), prefix.getPrefix());
      assertNull(prefix.getId());
    }
  }

  @Test
  void completeConfigurationReturnsEveryPersistedPrefix() {
    final Company company = Company.builder().id(2L).build();
    final List<DocumentPrefix> prefixes =
        java.util.Arrays.stream(DocumentType.values())
            .map(
                type ->
                    DocumentPrefix.builder()
                        .id((long) type.ordinal() + 1)
                        .company(company)
                        .documentType(type)
                        .prefix("P-" + type.name())
                        .build())
            .toList();
    when(repository.findAllByCompanyId(2L)).thenReturn(prefixes);

    final var response = service.getAllPrefixes();

    assertEquals(DocumentType.values().length, response.getPrefixes().size());
    for (final DocumentPrefix prefix : prefixes) {
      final var dto =
          response.getPrefixes().stream()
              .filter(item -> item.getDocumentType() == prefix.getDocumentType())
              .findFirst()
              .orElseThrow();
      assertEquals(prefix.getId(), dto.getId());
      assertEquals(prefix.getPrefix(), dto.getPrefix());
    }
  }

  @Test
  void partialConfigurationPreservesStoredValuesAndReturnsDefaultsForMissingTypes() {
    final Company company = Company.builder().id(2L).build();
    final DocumentPrefix existing =
        DocumentPrefix.builder()
            .id(4L)
            .company(company)
            .documentType(DocumentType.GOODS_RECEIPT)
            .prefix("CUSTOM")
            .build();
    when(repository.findAllByCompanyId(2L)).thenReturn(List.of(existing));

    final var response = service.getAllPrefixes();

    assertEquals(DocumentType.values().length, response.getPrefixes().size());
    for (final DocumentType type : DocumentType.values()) {
      final var dto =
          response.getPrefixes().stream()
              .filter(item -> item.getDocumentType() == type)
              .findFirst()
              .orElseThrow();
      if (type == DocumentType.GOODS_RECEIPT) {
        assertEquals(4L, dto.getId());
        assertEquals("CUSTOM", dto.getPrefix());
      } else {
        assertNull(dto.getId());
        assertEquals(type.getDefaultPrefix(), dto.getPrefix());
      }
    }
  }

  @Test
  void partialUpdateKeepsUnsubmittedRowsAndReturnsCompleteConfiguration() {
    final Company company = Company.builder().id(2L).build();
    final DocumentPrefix updated =
        DocumentPrefix.builder()
            .id(4L)
            .company(company)
            .documentType(DocumentType.GOODS_RECEIPT)
            .prefix("OLD")
            .build();
    final DocumentPrefix unchanged =
        DocumentPrefix.builder()
            .id(5L)
            .company(company)
            .documentType(DocumentType.PURCHASE_ORDER)
            .prefix("ORDER")
            .build();
    when(repository.findAllByCompanyId(2L)).thenReturn(List.of(updated, unchanged));
    when(repository.findByIdAndCompanyId(4L, 2L)).thenReturn(Optional.of(updated));
    when(repository.save(any(DocumentPrefix.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    final DocumentPrefixDto update =
        DocumentPrefixDto.builder()
            .id(4L)
            .documentType(DocumentType.GOODS_RECEIPT)
            .prefix("GR")
            .build();

    final var result = service.updatePrefixes(List.of(update));

    assertEquals("GR", updated.getPrefix());
    assertEquals("ORDER", unchanged.getPrefix());
    verify(repository).save(updated);
    assertEquals(DocumentType.values().length, result.getPrefixes().size());
    assertEquals(
        "ORDER",
        result.getPrefixes().stream()
            .filter(dto -> dto.getDocumentType() == DocumentType.PURCHASE_ORDER)
            .findFirst()
            .orElseThrow()
            .getPrefix());
    assertEquals(
        DocumentType.DELIVERY_NOTE.getDefaultPrefix(),
        result.getPrefixes().stream()
            .filter(dto -> dto.getDocumentType() == DocumentType.DELIVERY_NOTE)
            .findFirst()
            .orElseThrow()
            .getPrefix());
  }
}
