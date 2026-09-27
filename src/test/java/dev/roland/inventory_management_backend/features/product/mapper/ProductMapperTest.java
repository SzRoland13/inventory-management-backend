package dev.roland.inventory_management_backend.features.product.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import dev.roland.inventory_management_backend.features.brand.Brand;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.dto.ProductAttributeValueResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductStockResponse;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;
import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;
import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

@SpringJUnitConfig(classes = {ProductMapperImpl.class, ProductSettingsMapperImpl.class})
class ProductMapperTest {

  @Autowired private ProductMapper productMapper;

  @Test
  void updatesProductScalarsAndLeavesFacadeOwnedReferencesAndStatusUntouched() {
    final Unit existingUnit = unit(1L, "EA", "Each", "ea");
    final Currency existingCurrency = Currency.builder().id(3L).code("USD").build();
    final Product product =
        Product.builder()
            .unit(existingUnit)
            .currency(existingCurrency)
            .status(ProductStatus.ACTIVE)
            .build();
    final ProductRequest request =
        new ProductRequest(
            "SKU-1",
            "EAN-1",
            "Widget",
            "Product description",
            null,
            ProductStatus.DISCONTINUED,
            new ProductRequest.Units(10L, null, null),
            new ProductRequest.Pricing(
                20L, new BigDecimal("120.00"), new BigDecimal("80.00"), new BigDecimal("27.000")),
            new ProductRequest.Dimensions(
                new BigDecimal("2.000"),
                new BigDecimal("3.000"),
                new BigDecimal("4.000"),
                new BigDecimal("5.000")),
            List.of(7L),
            List.of());

    productMapper.updateScalarFields(request, product);

    assertEquals("SKU-1", product.getSku());
    assertEquals("EAN-1", product.getEan());
    assertEquals("Widget", product.getName());
    assertEquals("Product description", product.getDescription());
    assertEquals(null, product.getBrand());
    assertEquals(new BigDecimal("120.00"), product.getNetPrice());
    assertEquals(new BigDecimal("80.00"), product.getCostPrice());
    assertEquals(new BigDecimal("27.000"), product.getVatRate());
    assertEquals(new BigDecimal("2.000"), product.getWeight());
    assertEquals(new BigDecimal("3.000"), product.getWidth());
    assertEquals(new BigDecimal("4.000"), product.getHeight());
    assertEquals(new BigDecimal("5.000"), product.getDepth());
    assertSame(existingUnit, product.getUnit());
    assertSame(existingCurrency, product.getCurrency());
    assertEquals(ProductStatus.ACTIVE, product.getStatus());
  }

  @Test
  void mapsProductAndPreparedDataIntoNestedResponseObjects() {
    final Unit mainUnit = unit(1L, "BOX", "Box", "box");
    final Unit secondaryUnit = unit(2L, "PAIR", "Pair", "pair");
    final LocalDateTime createdAt = LocalDateTime.parse("2026-01-02T03:04:05");
    final LocalDateTime updatedAt = LocalDateTime.parse("2026-02-03T04:05:06");
    final Product product =
        Product.builder()
            .id(15L)
            .sku("SKU-15")
            .ean("EAN-15")
            .name("Gloves")
            .description("Work gloves")
            .brand(brand(7L, "Safety"))
            .status(ProductStatus.ACTIVE)
            .unit(mainUnit)
            .secondaryUnit(secondaryUnit)
            .secondaryUnitsPerMainUnit(new BigDecimal("12"))
            .currency(Currency.builder().id(5L).build())
            .netPrice(new BigDecimal("120.00"))
            .costPrice(new BigDecimal("75.00"))
            .vatRate(new BigDecimal("27.000"))
            .weight(new BigDecimal("1.500"))
            .width(new BigDecimal("2.000"))
            .height(new BigDecimal("3.000"))
            .depth(new BigDecimal("4.000"))
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    final ProductAttributeValueResponse attribute =
        new ProductAttributeValueResponse(
            8L,
            "COLOR",
            "Color",
            ProductAttributeValueType.TEXT,
            null,
            null,
            "Blue",
            null,
            null,
            null);
    final ProductStockResponse stock =
        new ProductStockResponse(
            4L,
            "Main warehouse",
            new ProductStockResponse.Quantity(
                new BigDecimal("35"), new UnitResponse(2L, "PAIR", "Pair", "pair", false)),
            new ProductStockResponse.StockBreakdown(
                new ProductStockResponse.Quantity(
                    new BigDecimal("2"), new UnitResponse(1L, "BOX", "Box", "box", false)),
                new ProductStockResponse.Quantity(
                    new BigDecimal("11"), new UnitResponse(2L, "PAIR", "Pair", "pair", false))));

    final ProductResponse response =
        productMapper.toProductResponse(
            product, List.of(9L), List.of(attribute), List.of(stock), new BigDecimal("10.00"));

    assertEquals(15L, response.id());
    assertEquals("SKU-15", response.sku());
    assertEquals(
        new ProductResponse.Units(
            new UnitResponse(1L, "BOX", "Box", "box", false),
            new UnitResponse(2L, "PAIR", "Pair", "pair", false),
            new BigDecimal("12")),
        response.units());
    assertEquals(
        new ProductResponse.Pricing(
            5L,
            new BigDecimal("120.00"),
            new BigDecimal("10.00"),
            new BigDecimal("75.00"),
            new BigDecimal("27.000")),
        response.pricing());
    assertEquals(
        new ProductResponse.Dimensions(
            new BigDecimal("1.500"),
            new BigDecimal("2.000"),
            new BigDecimal("3.000"),
            new BigDecimal("4.000")),
        response.dimensions());
    assertEquals(List.of(9L), response.categoryIds());
    assertEquals(List.of(attribute), response.attributes());
    assertEquals(List.of(stock), response.stockByWarehouse());
    assertEquals(createdAt, response.createdAt());
    assertEquals(updatedAt, response.updatedAt());
  }

  @Test
  void mapsAttributeValueEntityWithoutExposingItsRelationships() {
    final ProductAttributeDefinition definition =
        ProductAttributeDefinition.builder()
            .id(6L)
            .code("MATERIAL")
            .name("Material")
            .valueType(ProductAttributeValueType.FIXED)
            .build();
    final ProductAttributeOption option =
        ProductAttributeOption.builder().id(14L).definition(definition).value("Steel").build();
    final ProductAttributeValue value =
        ProductAttributeValue.builder().definition(definition).option(option).build();

    final ProductAttributeValueResponse response = productMapper.toAttributeValueResponse(value);

    assertEquals(
        new ProductAttributeValueResponse(
            6L,
            "MATERIAL",
            "Material",
            ProductAttributeValueType.FIXED,
            14L,
            "Steel",
            null,
            null,
            null,
            null),
        response);
  }

  @Test
  void mapsOptionalProductAndAttributeRelationshipsAsNulls() {
    final Product product =
        Product.builder()
            .id(20L)
            .sku("SKU-20")
            .name("Simple item")
            .unit(unit(1L, "EA", "Each", "ea"))
            .netPrice(BigDecimal.ONE)
            .vatRate(BigDecimal.ZERO)
            .build();

    final ProductResponse response =
        productMapper.toProductResponse(product, List.of(), List.of(), List.of(), null);

    assertEquals(null, response.units().secondary());
    assertEquals(null, response.units().secondaryUnitsPerMainUnit());
    assertEquals(null, response.pricing().currencyId());
    assertEquals(null, response.pricing().secondaryNetPrice());
    assertEquals(null, response.dimensions().weight());
    assertEquals(List.of(), response.categoryIds());
    assertEquals(List.of(), response.attributes());
    assertEquals(List.of(), response.stockByWarehouse());

    final ProductAttributeValue value =
        ProductAttributeValue.builder().textValue("Unlinked").build();
    final ProductAttributeValueResponse attribute = productMapper.toAttributeValueResponse(value);
    assertEquals(null, attribute.definitionId());
    assertEquals(null, attribute.optionId());
    assertEquals("Unlinked", attribute.textValue());
  }

  @Test
  void mapsStockQuantitiesAndWarehouseDetails() {
    final Unit mainUnit = unit(1L, "BOX", "Box", "box");
    final Unit secondaryUnit = unit(2L, "PAIR", "Pair", "pair");
    final ProductStockResponse.Quantity stockQuantity =
        productMapper.toStockQuantity(new BigDecimal("35"), secondaryUnit);
    final ProductStockResponse.Quantity fullMainUnits =
        productMapper.toStockQuantity(new BigDecimal("2"), mainUnit);
    final ProductStockResponse.Quantity remainder =
        productMapper.toStockQuantity(new BigDecimal("11"), secondaryUnit);
    final ProductStockResponse.StockBreakdown breakdown =
        productMapper.toStockBreakdown(fullMainUnits, remainder);
    final StockBalance balance =
        StockBalance.builder()
            .warehouse(Warehouse.builder().id(4L).name("Main warehouse").build())
            .build();

    final ProductStockResponse response =
        productMapper.toStockResponse(balance, stockQuantity, breakdown);

    assertEquals(4L, response.warehouseId());
    assertEquals("Main warehouse", response.warehouseName());
    assertEquals(stockQuantity, response.stockQuantity());
    assertEquals(breakdown, response.displayQuantity());
  }

  private Unit unit(final Long id, final String code, final String name, final String symbol) {
    return Unit.builder().id(id).code(code).name(name).symbol(symbol).system(false).build();
  }

  private Brand brand(final Long id, final String name) {
    final Brand brand = new Brand();
    brand.setId(id);
    brand.setName(name);
    return brand;
  }
}
