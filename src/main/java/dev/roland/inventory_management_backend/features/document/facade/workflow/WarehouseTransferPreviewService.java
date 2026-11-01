package dev.roland.inventory_management_backend.features.document.facade.workflow;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.mapper.WarehouseOperationPreviewAssembler;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import lombok.RequiredArgsConstructor;

/** Builds transfer dispatch, receipt, and transit resolution preview lines. */
@Service
@RequiredArgsConstructor
public class WarehouseTransferPreviewService {
  private final StockBalanceService balanceService;
  private final WarehouseOperationSupportService warehouseOperationSupportService;

  public void appendLines(
      final Document document,
      final Company company,
      final WarehouseOperationType operation,
      final WarehouseOperationRequest request,
      final List<WarehouseOperationPreview.Line> previewLines,
      final List<String> blockers) {
    for (var item : request.lines()) {
      final DocumentLine source =
          item.sourceLineId() == null
              ? null
              : warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
      final BigDecimal requested = DocumentWorkflowValues.nz(item.quantity());
      final BigDecimal transit =
          source == null
              ? BigDecimal.ZERO
              : DocumentWorkflowValues.nz(source.getInTransitQuantity());
      final boolean receipt = request.operation() == WarehouseOperationType.RECEIPT;
      final BigDecimal dispatched =
          source == null
              ? BigDecimal.ZERO
              : DocumentWorkflowValues.nz(source.getFulfilledQuantity());
      final BigDecimal remainingToDispatch =
          source == null
              ? BigDecimal.ZERO
              : DocumentWorkflowValues.nz(source.getQuantity()).subtract(dispatched);
      final BigDecimal available =
          source == null || document == null
              ? BigDecimal.ZERO
              : balanceService
                  .findByWarehouseAndProduct(document.getSourceWarehouse(), source.getProduct())
                  .map(balance -> DocumentWorkflowValues.nz(balance.getQuantity()))
                  .orElse(BigDecimal.ZERO);
      final boolean valid =
          source != null
              && document != null
              && source.getDocument().getId().equals(document.getId())
              && requested.signum() > 0
              && (operation == WarehouseOperationType.TRANSFER_RESOLUTION
                  ? requested.compareTo(transit) <= 0
                  : receipt
                      ? requested.compareTo(transit) <= 0
                      : requested.compareTo(remainingToDispatch) <= 0
                          && requested.compareTo(available) <= 0);
      if (!valid) blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
      previewLines.add(
          WarehouseOperationPreview.Line.builder()
              .sourceLineId(source == null ? item.sourceLineId() : source.getId())
              .productId(source == null ? item.productId() : source.getProduct().getId())
              .sourceQuantity(available)
              .transitQuantity(transit)
              .quantityDelta(requested.negate())
              .resultingQuantity(
                  receipt ? transit.subtract(requested) : available.subtract(requested))
              .errorMessageKey(valid ? null : DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey())
              .build());
    }
    WarehouseOperationPreviewAssembler.validateTransferRequest(operation, request, blockers);
  }
}
