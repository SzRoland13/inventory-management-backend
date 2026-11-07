package dev.roland.inventory_management_backend.features.document.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;

@ExtendWith(MockitoExtension.class)
class WarehouseOperationPreviewAssemblerTest {
  @Mock private ProductService productService;

  @Test
  void mapsUnmappedLinesWithoutDroppingRequestValues() {
    final var request =
        request(
            List.of(
                new WarehouseOperationRequest.WarehouseOperationLine(
                    4L,
                    7L,
                    new BigDecimal("3"),
                    new BigDecimal("4"),
                    null,
                    null,
                    new BigDecimal("5"),
                    null,
                    null,
                    null)),
            null,
            null);
    final List<WarehouseOperationPreview.Line> lines = new ArrayList<>();

    WarehouseOperationPreviewAssembler.addUnmappedLines(request, lines);

    assertEquals(1, lines.size());
    assertEquals(4L, lines.getFirst().sourceLineId());
    assertEquals(7L, lines.getFirst().productId());
    assertEquals(new BigDecimal("3"), lines.getFirst().quantityDelta());
    assertEquals(new BigDecimal("5"), lines.getFirst().countedQuantity());
  }

  @Test
  void addsBlockersForMissingOrUnsupportedTransferResolutionDetails() {
    final List<String> blockers = new ArrayList<>();
    WarehouseOperationPreviewAssembler.validateTransferRequest(
        WarehouseOperationType.TRANSFER_RESOLUTION,
        request(List.of(), WarehouseOperationType.DISPATCH, " "),
        blockers);

    assertEquals(
        List.of(
            DocumentApiMessageKey.DOCUMENT_REASON_REQUIRED.getKey(),
            DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey()),
        blockers);
  }

  @Test
  void addsBlockerWhenTransferPreviewOperationIsNotDispatchOrReceipt() {
    final List<String> blockers = new ArrayList<>();
    WarehouseOperationPreviewAssembler.validateTransferRequest(
        WarehouseOperationType.TRANSFER_PREVIEW,
        request(List.of(), WarehouseOperationType.LOSS, "reason"),
        blockers);

    assertEquals(List.of(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey()), blockers);
  }

  @Test
  void finishesPreviewAndAddsResolvedProductNamesAndDuplicateLineBlocker() {
    final Company company = Company.builder().id(10L).build();
    final Product product = Product.builder().id(7L).name("Widget").build();
    when(productService.findByIdAndCompanyId(7L, 10L)).thenReturn(Optional.of(product));
    final var request =
        request(
            List.of(
                new WarehouseOperationRequest.WarehouseOperationLine(
                    3L, 7L, null, null, null, null, null, null, null, null),
                new WarehouseOperationRequest.WarehouseOperationLine(
                    3L, null, null, null, null, null, null, null, null, null)),
            null,
            null);
    final List<WarehouseOperationPreview.Line> lines =
        new ArrayList<>(
            List.of(
                WarehouseOperationPreview.Line.builder().productId(7L).build(),
                WarehouseOperationPreview.Line.builder().productId(null).build()));

    final WarehouseOperationPreview preview =
        WarehouseOperationPreviewAssembler.finish(
            company,
            productService,
            request,
            lines,
            new ArrayList<>(),
            "version",
            List.of(),
            List.of());

    assertFalse(preview.valid());
    assertEquals(
        DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey(), preview.blockers().getFirst());
    assertEquals("Widget", preview.lines().getFirst().productName());
    assertEquals(null, preview.lines().get(1).productName());
  }

  @Test
  void finishLeavesPreviewValidWhenThereAreNoBlockers() {
    final Company company = Company.builder().id(10L).build();
    final var request = request(List.of(), null, null);

    final WarehouseOperationPreview preview =
        WarehouseOperationPreviewAssembler.finish(
            company,
            productService,
            request,
            List.of(),
            new ArrayList<>(),
            "v1",
            List.of(),
            List.of());

    assertTrue(preview.valid());
    assertEquals("v1", preview.calculationVersion());
  }

  private WarehouseOperationRequest request(
      final List<WarehouseOperationRequest.WarehouseOperationLine> lines,
      final WarehouseOperationType operation,
      final String reason) {
    return new WarehouseOperationRequest(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        LocalDate.now(),
        (LocalDateTime) null,
        null,
        null,
        null,
        operation,
        null,
        reason,
        null,
        false,
        lines);
  }
}
