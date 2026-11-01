package dev.roland.inventory_management_backend.features.document.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.unit.Unit;

class DocumentLineMapperTest {
  @Test
  void copiesIssuedSnapshotsEvenWhenTheCatalogHasChanged() {
    var product = Product.builder().name("Renamed product").sku("NEW").build();
    var source =
        DocumentLine.builder()
            .product(product)
            .productNameSnapshot("Issued name")
            .productSkuSnapshot("OLD")
            .unitNameSnapshot("Issued unit")
            .unitCodeSnapshot("BOX")
            .conversionFactorSnapshot(new BigDecimal("12"))
            .unitPrice(new BigDecimal("4.50"))
            .sourceUnitPrice(new BigDecimal("5"))
            .exchangeRateSnapshot(new BigDecimal("0.9"))
            .build();
    var target = new Document();
    var copied = DocumentLineMapper.copyOperationLine(target, source, new BigDecimal("2"));
    assertSame(target, copied.getDocument());
    assertEquals("Issued name", copied.getProductNameSnapshot());
    assertEquals("OLD", copied.getProductSkuSnapshot());
    assertEquals("Issued unit", copied.getUnitNameSnapshot());
    assertEquals(new BigDecimal("12"), copied.getConversionFactorSnapshot());
    assertEquals(new BigDecimal("9.00"), copied.getTotalPrice());
    assertEquals(new BigDecimal("0.9"), copied.getExchangeRateSnapshot());
    assertEquals(BigDecimal.ZERO, copied.getFulfilledQuantity());
  }

  @Test
  void newLinesSnapshotMainUnitConversionAndCatalogLabels() {
    var unit = Unit.builder().id(1L).name("Box").code("BOX").build();
    var secondary = Unit.builder().id(2L).build();
    var product =
        Product.builder()
            .name("Item")
            .sku("SKU")
            .unit(unit)
            .secondaryUnit(secondary)
            .secondaryUnitsPerMainUnit(new BigDecimal("12"))
            .build();
    var line =
        DocumentLineMapper.buildLine(
            new Document(), product, unit, new BigDecimal("3"), new BigDecimal("2"));
    product.setName("Renamed");
    unit.setName("Renamed unit");
    assertEquals("Item", line.getProductNameSnapshot());
    assertEquals("Box", line.getUnitNameSnapshot());
    assertEquals(new BigDecimal("12"), line.getConversionFactorSnapshot());
    assertEquals(new BigDecimal("6"), line.getTotalPrice());
  }
}
