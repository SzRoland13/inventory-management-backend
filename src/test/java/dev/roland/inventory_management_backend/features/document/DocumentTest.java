package dev.roland.inventory_management_backend.features.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

class DocumentTest {

  @Test
  void snapshotsWarehouseCodesOnFirstPersistOnly() {
    final Warehouse source = Warehouse.builder().code("SRC").build();
    final Warehouse target = Warehouse.builder().code("DST").build();
    final Document document =
        Document.builder().sourceWarehouse(source).targetWarehouse(target).build();

    document.initializeWarehouseCodeSnapshots();
    source.setCode("SRC-UPDATED");
    target.setCode("DST-UPDATED");
    document.initializeWarehouseCodeSnapshots();

    assertEquals("SRC", document.getSourceWarehouseCodeSnapshot());
    assertEquals("DST", document.getTargetWarehouseCodeSnapshot());
  }
}
