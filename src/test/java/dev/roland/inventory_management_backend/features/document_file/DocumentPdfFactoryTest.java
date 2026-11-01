package dev.roland.inventory_management_backend.features.document_file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;

import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;

@ExtendWith(MockitoExtension.class)
class DocumentPdfFactoryTest {
  @Mock private MessageSource documentMessages;
  private DocumentPdfFactory factory;
  @Mock private ITemplateEngine templateEngine;

  @BeforeEach
  void setUp() {
    factory = new DocumentPdfFactory(templateEngine, documentMessages);
  }

  @Test
  void createsEnglishHtmlForAnEmptyDraft() {
    final Document document = document(List.of());
    when(document.getDocumentNumber()).thenReturn(null);
    when(document.getPartnerNameSnapshot()).thenReturn(null);
    when(document.getPartnerAddressSnapshot()).thenReturn(null);
    when(document.getSourceWarehouseNameSnapshot()).thenReturn(null);
    when(document.getTargetWarehouseNameSnapshot()).thenReturn(null);
    when(document.getNotes()).thenReturn(null);
    when(document.getCurrency()).thenReturn(null);
    when(documentMessages.getMessage(
            anyString(), isNull(), org.mockito.ArgumentMatchers.any(Locale.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(templateEngine.process(eq("documents/document"), any(IContext.class)))
        .thenReturn("rendered");
    assertEquals("rendered", factory.createHtml(document, null));
    final IContext context = capturedContext();
    assertEquals("pdf.document.draft", context.getVariable("number"));
    assertEquals(List.of(), context.getVariable("lines"));
    assertEquals("", context.getVariable("partnerName"));
  }

  @Test
  void rendersLinesUsingTheSelectedLocaleAndSourceWarehouseName() {
    final DocumentLine line =
        DocumentLine.builder()
            .quantity(new BigDecimal("2.5"))
            .unitPrice(new BigDecimal("4"))
            .vatRateSnapshot(new BigDecimal("27"))
            .productNameSnapshot("Widget")
            .productSkuSnapshot("W-1")
            .unitNameSnapshot("pcs")
            .build();
    final Document document = document(List.of(line));
    when(document.getDocumentNumber()).thenReturn("PO-9");
    when(document.getPartnerNameSnapshot()).thenReturn("Partner");
    when(document.getPartnerAddressSnapshot()).thenReturn("Address");
    when(document.getSourceWarehouseNameSnapshot()).thenReturn("Source");
    when(document.getNotes()).thenReturn("Note");
    when(document.getCurrency()).thenReturn(null);
    when(documentMessages.getMessage(
            anyString(), isNull(), org.mockito.ArgumentMatchers.any(Locale.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(templateEngine.process(eq("documents/document"), any(IContext.class)))
        .thenReturn("rendered");
    assertEquals("rendered", factory.createHtml(document, "hu"));
    final IContext context = capturedContext();
    assertEquals("Source", context.getVariable("warehouseName"));
    final List<?> rows = (List<?>) context.getVariable("lines");
    final java.util.Map<?, ?> firstLine = (java.util.Map<?, ?>) rows.getFirst();
    assertEquals("Widget", firstLine.get("product"));
    assertEquals("10,00", firstLine.get("net"));
    assertEquals("2,70", firstLine.get("tax"));
    assertEquals("12,70", firstLine.get("gross"));
  }

  @Test
  void defaultsMissingLineValuesAndFallsBackToTargetWarehouse() {
    final DocumentLine line = DocumentLine.builder().build();
    final Document document = document(List.of(line));
    when(document.getDocumentNumber()).thenReturn("GR-1");
    when(document.getTargetWarehouseNameSnapshot()).thenReturn("Receiving warehouse");
    when(documentMessages.getMessage(
            anyString(), isNull(), org.mockito.ArgumentMatchers.any(Locale.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(templateEngine.process(eq("documents/document"), any(IContext.class)))
        .thenReturn("rendered");

    factory.createHtml(document, "en");

    final IContext context = capturedContext();
    assertEquals("Receiving warehouse", context.getVariable("warehouseName"));
    final java.util.Map<?, ?> row =
        (java.util.Map<?, ?>) ((List<?>) context.getVariable("lines")).getFirst();
    assertEquals("0.00", row.get("net"));
    assertEquals("0.00", row.get("tax"));
    assertEquals("0.00", row.get("gross"));
  }

  private IContext capturedContext() {
    final ArgumentCaptor<IContext> captor = ArgumentCaptor.forClass(IContext.class);
    org.mockito.Mockito.verify(templateEngine).process(eq("documents/document"), captor.capture());
    return captor.getValue();
  }

  private static Document document(final List<DocumentLine> lines) {
    final Document document = org.mockito.Mockito.mock(Document.class);
    when(document.getType()).thenReturn(DocumentType.PURCHASE_ORDER);
    when(document.getStatus()).thenReturn(DocumentStatus.DRAFT);
    when(document.getDocumentDate()).thenReturn(LocalDate.of(2026, 10, 31));
    when(document.getLines()).thenReturn(lines);
    return document;
  }
}
