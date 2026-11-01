package dev.roland.inventory_management_backend.features.document.mapper;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import lombok.experimental.UtilityClass;

/** Builds the common response envelope for warehouse operation previews. */
@UtilityClass
public final class WarehouseOperationPreviewAssembler {
  public static void addUnmappedLines(
      final WarehouseOperationRequest request,
      final List<WarehouseOperationPreview.Line> previewLines) {
    for (var item : request.lines()) {
      previewLines.add(
          new WarehouseOperationPreview.Line(
              item.sourceLineId(),
              item.productId(),
              null,
              null,
              null,
              null,
              item.countedQuantity(),
              null,
              null,
              null,
              null,
              item.correctedQuantity(),
              null,
              null,
              null,
              null,
              null,
              item.quantity(),
              null,
              null,
              null));
    }
  }

  public static void validateTransferRequest(
      final WarehouseOperationType operation,
      final WarehouseOperationRequest request,
      final List<String> blockers) {
    if (operation == WarehouseOperationType.TRANSFER_RESOLUTION
        && (request.reason() == null || request.reason().isBlank())) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_REASON_REQUIRED.getKey());
    }
    if (operation == WarehouseOperationType.TRANSFER_RESOLUTION
        && !List.of(
                WarehouseOperationType.LOSS,
                WarehouseOperationType.DAMAGE,
                WarehouseOperationType.REVERSAL,
                WarehouseOperationType.REMAINDER_CLOSURE)
            .contains(request.operation())) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
    }
    if (operation == WarehouseOperationType.TRANSFER_PREVIEW
        && !List.of(WarehouseOperationType.DISPATCH, WarehouseOperationType.RECEIPT)
            .contains(request.operation())) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
    }
  }

  private static boolean hasDuplicateSourceLines(
      final List<WarehouseOperationRequest.WarehouseOperationLine> lines) {
    final Set<Long> ids = new HashSet<>();
    return lines != null
        && lines.stream()
            .map(WarehouseOperationRequest.WarehouseOperationLine::sourceLineId)
            .filter(Objects::nonNull)
            .anyMatch(id -> !ids.add(id));
  }

  public static WarehouseOperationPreview finish(
      final Company company,
      final ProductService productService,
      final WarehouseOperationRequest request,
      final List<WarehouseOperationPreview.Line> previewLines,
      final List<String> blockers,
      final String calculationVersion,
      final List<WarehouseOperationPreview.Movement> movements,
      final List<WarehouseOperationPreview.DocumentRef> relatedReturns) {
    if (request != null && hasDuplicateSourceLines(request.lines()))
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
    final List<WarehouseOperationPreview.Line> namedLines =
        previewLines.stream()
            .map(
                line ->
                    line.withProductName(
                        line.productId() == null
                            ? null
                            : productService
                                .findByIdAndCompanyId(line.productId(), company.getId())
                                .map(Product::getName)
                                .orElse(null)))
            .toList();
    return new WarehouseOperationPreview(
        blockers.isEmpty(), calculationVersion, namedLines, blockers, movements, relatedReturns);
  }
}
