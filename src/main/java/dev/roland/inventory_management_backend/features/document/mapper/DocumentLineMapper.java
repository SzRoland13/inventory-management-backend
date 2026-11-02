package dev.roland.inventory_management_backend.features.document.mapper;

import java.math.BigDecimal;

import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.unit.Unit;

/** Creates line snapshots without persistence side effects. */
public final class DocumentLineMapper {
  private DocumentLineMapper() {}

  /**
   * Builds a document line with product and unit snapshots.
   *
   * @param doc the doc value
   * @param price the price value
   * @param product the product value
   * @param quantity the quantity value
   * @param unit the unit value
   * @return the resulting value
   */
  public static DocumentLine buildLine(
      final Document doc,
      final Product product,
      final Unit unit,
      final BigDecimal quantity,
      final BigDecimal price) {
    return DocumentLine.builder()
        .document(doc)
        .product(product)
        .unitSnapshot(unit)
        .unitCodeSnapshot(unit.getCode())
        .unitNameSnapshot(unit.getName())
        .conversionFactorSnapshot(DocumentWorkflowValues.expectedFactor(product, unit))
        .quantity(quantity)
        .fulfilledQuantity(BigDecimal.ZERO)
        .unitPrice(price)
        .sourceUnitPrice(price)
        .totalPrice(price.multiply(quantity))
        .productNameSnapshot(product.getName())
        .productSkuSnapshot(product.getSku())
        .vatRateSnapshot(product.getVatRate())
        .build();
  }

  /**
   * Copies a source line into a warehouse operation document.
   *
   * @param doc the doc value
   * @param quantity the quantity value
   * @param source the source value
   * @return the resulting value
   */
  public static DocumentLine copyOperationLine(
      final Document doc, final DocumentLine source, final BigDecimal quantity) {
    return DocumentLine.builder()
        .document(doc)
        .product(source.getProduct())
        .unitSnapshot(source.getUnitSnapshot())
        .unitCodeSnapshot(source.getUnitCodeSnapshot())
        .unitNameSnapshot(source.getUnitNameSnapshot())
        .conversionFactorSnapshot(source.getConversionFactorSnapshot())
        .quantity(quantity)
        .fulfilledQuantity(BigDecimal.ZERO)
        .unitPrice(DocumentWorkflowValues.nz(source.getUnitPrice()))
        .sourceUnitPrice(DocumentWorkflowValues.nz(source.getSourceUnitPrice()))
        .totalPrice(DocumentWorkflowValues.nz(source.getUnitPrice()).multiply(quantity))
        .currency(source.getCurrency())
        .sourceCurrency(source.getSourceCurrency())
        .exchangeRateSnapshot(source.getExchangeRateSnapshot())
        .productNameSnapshot(source.getProductNameSnapshot())
        .productSkuSnapshot(source.getProductSkuSnapshot())
        .vatRateSnapshot(source.getVatRateSnapshot())
        .build();
  }
}
