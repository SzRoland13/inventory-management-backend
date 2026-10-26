package dev.roland.inventory_management_backend.features.stock_balance.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.repository.CompanyRepository;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.product.repository.ProductRepository;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.dto.StockBalanceQueryRow;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementQueryRow;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.repository.StockMovementRepository;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.repository.UnitRepository;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import dev.roland.inventory_management_backend.features.warehouse.repository.WarehouseRepository;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StockReadRepositoryTest {
  @Autowired private StockBalanceRepository stockBalanceRepository;
  @Autowired private StockMovementRepository stockMovementRepository;
  @Autowired private CompanyRepository companyRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private UnitRepository unitRepository;
  @Autowired private WarehouseRepository warehouseRepository;

  private Company company;
  private Product product;
  private Warehouse warehouse;

  @BeforeEach
  void setUp() {
    company = companyRepository.save(Company.builder().name("Acme").build());
    final Unit unit =
        unitRepository.save(
            Unit.builder().code("EA").name("Each").symbol("ea").system(true).build());
    product =
        productRepository.save(
            Product.builder()
                .company(company)
                .sku("SKU-1")
                .name("Widget")
                .status(ProductStatus.ACTIVE)
                .unit(unit)
                .netPrice(BigDecimal.ONE)
                .vatRate(BigDecimal.ZERO)
                .build());
    warehouse =
        warehouseRepository.save(
            Warehouse.builder()
                .company(company)
                .name("Main")
                .code("MAIN")
                .type(WarehouseType.STANDARD)
                .status(WarehouseStatus.ACTIVE)
                .build());
  }

  @Test
  void balanceQueryReturnsZeroPairAndScopesToActiveCompanyEntities() {
    final Page<StockBalanceQueryRow> result =
        stockBalanceRepository.findCompanyStockRows(
            company.getId(),
            ProductStatus.ACTIVE,
            WarehouseStatus.ACTIVE,
            null,
            null,
            null,
            PageRequest.of(0, 25));

    assertEquals(1, result.getTotalElements());
    assertEquals(BigDecimal.ZERO, result.getContent().getFirst().getQuantity());
    assertEquals(product.getId(), result.getContent().getFirst().getProductId());
    assertEquals(warehouse.getId(), result.getContent().getFirst().getWarehouseId());

    stockBalanceRepository.save(
        StockBalance.builder()
            .product(product)
            .warehouse(warehouse)
            .quantity(new BigDecimal("7.5"))
            .build());

    final Page<StockBalanceQueryRow> persisted =
        stockBalanceRepository.findCompanyStockRows(
            company.getId(),
            ProductStatus.ACTIVE,
            WarehouseStatus.ACTIVE,
            product.getId(),
            warehouse.getId(),
            "wid",
            PageRequest.of(0, 25));
    assertEquals(1, persisted.getTotalElements());
    assertEquals(new BigDecimal("7.50000000"), persisted.getContent().getFirst().getQuantity());
  }

  @Test
  void movementQueryHandlesEveryDateFilterCombination() {
    final StockMovement movement =
        stockMovementRepository.saveAndFlush(
            StockMovement.builder()
                .product(product)
                .warehouse(warehouse)
                .quantityChange(new BigDecimal("-3.25"))
                .movementType(StockMovementType.SALE_OUTBOUND)
                .build());

    final LocalDateTime createdAt = movement.getCreatedAt();
    assertNotNull(createdAt);
    final LocalDateTime startDate = createdAt.minusSeconds(1);
    final LocalDateTime endDate = createdAt.plusSeconds(1);

    final Page<StockMovementQueryRow> withoutDates =
        stockMovementRepository.findCompanyMovements(
            company.getId(), null, null, null, null, PageRequest.of(0, 25));
    final Page<StockMovementQueryRow> withStartOnly =
        stockMovementRepository.findCompanyMovements(
            company.getId(), null, null, startDate, null, PageRequest.of(0, 25));
    final Page<StockMovementQueryRow> withEndOnly =
        stockMovementRepository.findCompanyMovements(
            company.getId(), null, null, null, endDate, PageRequest.of(0, 25));
    final Page<StockMovementQueryRow> withFullRange =
        stockMovementRepository.findCompanyMovements(
            company.getId(), null, null, startDate, endDate, PageRequest.of(0, 25));

    assertEquals(1, withoutDates.getTotalElements());
    assertEquals(1, withStartOnly.getTotalElements());
    assertEquals(1, withEndOnly.getTotalElements());
    assertEquals(1, withFullRange.getTotalElements());
    assertEquals(movement.getId(), withFullRange.getContent().getFirst().getId());
    assertEquals(
        new BigDecimal("-3.25000000"), withFullRange.getContent().getFirst().getQuantityChange());
    assertEquals("EA", withFullRange.getContent().getFirst().getMainUnitCode());
    assertEquals(createdAt, withFullRange.getContent().getFirst().getDate());
  }
}
