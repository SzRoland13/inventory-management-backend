package dev.roland.inventory_management_backend.features.document.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.unit.Unit;

class DocumentWorkflowValuesTest {
  private static final BigDecimal TEN = new BigDecimal("10");

  @Test
  void resolvesConfiguredUnitsAndConvertsQuantities() {
    final Unit box = Unit.builder().id(1L).code("BOX").name("Box").build();
    final Unit pair = Unit.builder().id(2L).code("PAIR").name("Pair").build();
    final Unit pallet = Unit.builder().id(3L).code("PALLET").name("Pallet").build();
    final Unit crate = Unit.builder().id(4L).code("CRATE").name("Crate").build();
    final Product product =
        Product.builder()
            .id(4L)
            .unit(box)
            .secondaryUnit(pair)
            .secondaryUnitsPerMainUnit(TEN)
            .build();
    final DocumentLine source = line(product, pallet, TEN);

    assertEquals(TEN, DocumentWorkflowValues.expectedFactor(product, box));
    assertEquals(BigDecimal.ONE, DocumentWorkflowValues.expectedFactor(product, pair));
    assertEquals(BigDecimal.ONE, DocumentWorkflowValues.expectedFactor(new Product(), null));
    assertEquals(box, DocumentWorkflowValues.configuredUnit(product, null));
    assertEquals(box, DocumentWorkflowValues.configuredUnit(product, box.getId()));
    assertEquals(pair, DocumentWorkflowValues.configuredUnit(product, pair.getId()));
    assertEquals(new BigDecimal("100"), DocumentWorkflowValues.stockQuantity(source, TEN));
    assertEquals(
        new BigDecimal("0.10000000"), DocumentWorkflowValues.entryQuantity(source, BigDecimal.ONE));
    assertEquals(TEN, DocumentWorkflowValues.operationFactor(source, box.getId()));
    assertEquals(BigDecimal.ONE, DocumentWorkflowValues.operationFactor(source, pair.getId()));
    assertEquals(TEN, DocumentWorkflowValues.operationFactor(source, null));
    assertEquals(BigDecimal.ONE, DocumentWorkflowValues.operationFactor(null, box.getId()));

    final DocumentLine target = new DocumentLine();
    DocumentWorkflowValues.applyOperationUnit(target, source, box.getId());
    assertEquals(box, target.getUnitSnapshot());
    assertEquals(TEN, target.getConversionFactorSnapshot());
    DocumentWorkflowValues.applyOperationUnit(target, source, pair.getId());
    assertEquals(pair, target.getUnitSnapshot());
    assertEquals(BigDecimal.ONE, target.getConversionFactorSnapshot());
    DocumentWorkflowValues.applyOperationUnit(target, source, null);
    DocumentWorkflowValues.applyOperationUnit(target, null, box.getId());

    assertThrows(ApiException.class, () -> DocumentWorkflowValues.expectedFactor(product, crate));
    assertThrows(
        ApiException.class, () -> DocumentWorkflowValues.configuredUnit(product, crate.getId()));
    assertThrows(
        ApiException.class, () -> DocumentWorkflowValues.operationFactor(source, crate.getId()));
    assertThrows(
        ApiException.class,
        () -> DocumentWorkflowValues.applyOperationUnit(target, source, crate.getId()));
  }

  @Test
  void supportsLegacyLinesWithoutSnapshotsAndProductsWithoutUnits() {
    final Unit box = Unit.builder().id(11L).code("BOX").name("Box").build();
    final Product product = Product.builder().id(12L).unit(box).build();
    final DocumentLine source = line(product, box, null);

    assertEquals(BigDecimal.ONE, DocumentWorkflowValues.operationFactor(null, null));
    assertEquals(BigDecimal.ONE, DocumentWorkflowValues.stockQuantity(source, BigDecimal.ONE));
    assertEquals(BigDecimal.ONE, DocumentWorkflowValues.expectedFactor(product, box));
    assertNull(DocumentWorkflowValues.configuredUnit(new Product(), null));
  }

  private DocumentLine line(final Product product, final Unit unit, final BigDecimal factor) {
    final DocumentLine line = new DocumentLine();
    line.setProduct(product);
    line.setUnitSnapshot(unit);
    line.setConversionFactorSnapshot(factor);
    return line;
  }
}
