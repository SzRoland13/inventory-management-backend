package dev.roland.inventory_management_backend.features.document_line;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.document_line.message.DocumentLineMessageKey;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.unit.Unit;

class DocumentLineTest {
  private static final BigDecimal FACTOR = new BigDecimal("12");

  @Test
  void initializesMainUnitLineWithConfiguredConversionFactorAndProductSnapshots() {
    Unit main = unit(1L, "BOX", "Box");
    Product product = product(main, unit(2L, "PAIR", "Pair"));
    DocumentLine line = DocumentLine.builder().product(product).build();

    line.initializeUnitSnapshot();

    assertEquals(main, line.getUnitSnapshot());
    assertEquals("BOX", line.getUnitCodeSnapshot());
    assertEquals("Box", line.getUnitNameSnapshot());
    assertEquals(0, FACTOR.compareTo(line.getConversionFactorSnapshot()));
    assertEquals(product.getName(), line.getProductNameSnapshot());
    assertEquals(product.getSku(), line.getProductSkuSnapshot());
    assertEquals(product.getVatRate(), line.getVatRateSnapshot());
  }

  @Test
  void initializesSecondaryUnitLineWithOneToOneFactor() {
    Unit secondary = unit(2L, "PAIR", "Pair");
    DocumentLine line =
        DocumentLine.builder()
            .product(product(unit(1L, "BOX", "Box"), secondary))
            .unitSnapshot(secondary)
            .build();

    line.initializeUnitSnapshot();

    assertEquals(BigDecimal.ONE, line.getConversionFactorSnapshot());
    assertEquals("PAIR", line.getUnitCodeSnapshot());
  }

  @Test
  void preservesMatchingProvidedSnapshots() {
    Unit main = unit(1L, "BOX", "Box");
    DocumentLine line =
        DocumentLine.builder()
            .product(product(main, null))
            .unitSnapshot(main)
            .unitCodeSnapshot("BOX-OLD")
            .unitNameSnapshot("Historical box")
            .conversionFactorSnapshot(BigDecimal.ONE)
            .productNameSnapshot("Historical product")
            .productSkuSnapshot("OLD-SKU")
            .vatRateSnapshot(new BigDecimal("5"))
            .build();

    line.initializeUnitSnapshot();

    assertEquals("BOX-OLD", line.getUnitCodeSnapshot());
    assertEquals("Historical box", line.getUnitNameSnapshot());
    assertEquals("Historical product", line.getProductNameSnapshot());
    assertEquals("OLD-SKU", line.getProductSkuSnapshot());
    assertEquals(new BigDecimal("5"), line.getVatRateSnapshot());
  }

  @Test
  void rejectsMissingProductAndUnit() {
    ApiException missingProduct =
        assertThrows(ApiException.class, () -> new DocumentLine().initializeUnitSnapshot());
    assertEquals(DocumentLineMessageKey.PRODUCT_REQUIRED, missingProduct.getMessageKey());

    ApiException missingUnit =
        assertThrows(
            ApiException.class,
            () -> DocumentLine.builder().product(new Product()).build().initializeUnitSnapshot());
    assertEquals(DocumentLineMessageKey.UNIT_REQUIRED, missingUnit.getMessageKey());
  }

  @Test
  void rejectsUnsupportedUnitAndInvalidConversionFactor() {
    Product product = product(unit(1L, "BOX", "Box"), unit(2L, "PAIR", "Pair"));
    DocumentLine unsupported =
        DocumentLine.builder().product(product).unitSnapshot(unit(3L, "EA", "Each")).build();
    ApiException invalidUnit =
        assertThrows(ApiException.class, unsupported::initializeUnitSnapshot);
    assertEquals(DocumentLineMessageKey.INVALID_UNIT, invalidUnit.getMessageKey());

    DocumentLine wrongFactor =
        DocumentLine.builder()
            .product(product)
            .unitSnapshot(product.getUnit())
            .conversionFactorSnapshot(BigDecimal.ONE)
            .build();
    ApiException mismatchedFactor =
        assertThrows(ApiException.class, wrongFactor::initializeUnitSnapshot);
    assertEquals(
        DocumentLineMessageKey.INVALID_CONVERSION_FACTOR, mismatchedFactor.getMessageKey());

    DocumentLine nonPositive =
        DocumentLine.builder()
            .product(product)
            .unitSnapshot(product.getSecondaryUnit())
            .conversionFactorSnapshot(BigDecimal.ZERO)
            .build();
    ApiException zeroFactor = assertThrows(ApiException.class, nonPositive::initializeUnitSnapshot);
    assertEquals(DocumentLineMessageKey.INVALID_CONVERSION_FACTOR, zeroFactor.getMessageKey());

    Product invalidProduct = product(unit(4L, "CASE", "Case"), unit(5L, "ITEM", "Item"));
    invalidProduct.setSecondaryUnitsPerMainUnit(BigDecimal.ZERO);
    DocumentLine invalidConfiguration =
        DocumentLine.builder()
            .product(invalidProduct)
            .unitSnapshot(invalidProduct.getUnit())
            .build();
    ApiException invalidExpectedFactor =
        assertThrows(ApiException.class, invalidConfiguration::initializeUnitSnapshot);
    assertEquals(
        DocumentLineMessageKey.INVALID_CONVERSION_FACTOR, invalidExpectedFactor.getMessageKey());
  }

  private static Product product(Unit main, Unit secondary) {
    return Product.builder()
        .unit(main)
        .secondaryUnit(secondary)
        .secondaryUnitsPerMainUnit(secondary == null ? null : FACTOR)
        .name("Widget")
        .sku("W-1")
        .vatRate(new BigDecimal("27"))
        .build();
  }

  private static Unit unit(Long id, String code, String name) {
    return Unit.builder().id(id).code(code).name(name).symbol(code).build();
  }
}
