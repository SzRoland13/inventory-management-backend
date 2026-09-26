package dev.roland.inventory_management_backend.features.product.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import dev.roland.inventory_management_backend.common.dto.PageResponse;
import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.currency.service.CurrencyService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.dto.ProductAttributeValueRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductAttributeValueResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductListRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductStockResponse;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.product.mapper.ProductMapper;
import dev.roland.inventory_management_backend.features.product.message.ProductMessageKey;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;
import dev.roland.inventory_management_backend.features.product_attribute_definition.service.ProductAttributeDefinitionService;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;
import dev.roland.inventory_management_backend.features.product_attribute_option.service.ProductAttributeOptionService;
import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;
import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;
import dev.roland.inventory_management_backend.features.product_attribute_value.service.ProductAttributeValueService;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;
import dev.roland.inventory_management_backend.features.product_category.service.ProductCategoryService;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignment;
import dev.roland.inventory_management_backend.features.product_category_assignment.service.ProductCategoryAssignmentService;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.service.UnitService;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

@ExtendWith(MockitoExtension.class)
class ProductManagementFacadeImplTest {

  @Mock private ProductService productService;
  @Mock private ProductMapper productMapper;
  @Mock private CompanyService companyService;
  @Mock private UnitService unitService;
  @Mock private CurrencyService currencyService;
  @Mock private ProductCategoryService categoryService;
  @Mock private ProductCategoryAssignmentService assignmentService;
  @Mock private ProductAttributeDefinitionService definitionService;
  @Mock private ProductAttributeOptionService optionService;
  @Mock private ProductAttributeValueService valueService;
  @Mock private StockMovementService movementService;
  @Mock private StockBalanceService stockBalanceService;

  private ProductManagementFacadeImpl facade;
  private Company company;

  @BeforeEach
  void setUp() {
    facade =
        new ProductManagementFacadeImpl(
            productService,
            productMapper,
            companyService,
            unitService,
            currencyService,
            categoryService,
            assignmentService,
            definitionService,
            optionService,
            valueService,
            movementService,
            stockBalanceService);
    company = Company.builder().id(1L).build();
  }

  @Test
  void createsProductWithResolvedReferencesAndNormalizedSku() {
    final ProductRequest request = productRequest(null, null, List.of(), List.of());
    final Unit mainUnit = unit(10L, "EA", true, null);
    final ProductResponse response = response(100L);
    prepareCreate(request, mainUnit, response);

    final ProductResponse result = facade.create(request);

    assertEquals(response, result);
    final ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
    verify(productService).save(productCaptor.capture());
    assertEquals("SKU-1", productCaptor.getValue().getSku());
    assertEquals(ProductStatus.ACTIVE, productCaptor.getValue().getStatus());
    assertEquals(mainUnit, productCaptor.getValue().getUnit());
    assertEquals(company, productCaptor.getValue().getCompany());
  }

  @Test
  void rejectsDuplicateSkuAndArchivedStatusOnCreation() {
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.existsByCompanyIdAndSku(1L, "SKU-1")).thenReturn(true);
    final ApiException duplicate =
        assertThrows(
            ApiException.class,
            () -> facade.create(productRequest(null, null, List.of(), List.of())));
    assertEquals(ProductMessageKey.PRODUCT_SKU_ALREADY_EXISTS, duplicate.getMessageKey());

    when(productService.existsByCompanyIdAndSku(1L, "SKU-1")).thenReturn(false);
    final ApiException archivedStatus =
        assertThrows(
            ApiException.class,
            () ->
                facade.create(productRequest(ProductStatus.ARCHIVED, null, List.of(), List.of())));
    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, archivedStatus.getMessageKey());
    verify(productService, never()).save(any(Product.class));
  }

  @Test
  void rejectsACompanyUnitOwnedByAnotherCompany() {
    final ProductRequest request = productRequest(null, null, List.of(), List.of());
    final Company otherCompany = Company.builder().id(2L).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.existsByCompanyIdAndSku(1L, "SKU-1")).thenReturn(false);
    when(unitService.findById(10L)).thenReturn(Optional.of(unit(10L, "EA", false, otherCompany)));

    final ApiException exception = assertThrows(ApiException.class, () -> facade.create(request));

    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, exception.getMessageKey());
    verify(productService, never()).save(any(Product.class));
  }

  @Test
  void rejectsACompanyUnitWithoutAnOwner() {
    final ProductRequest request = productRequest(null, null, List.of(), List.of());
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.existsByCompanyIdAndSku(1L, "SKU-1")).thenReturn(false);
    when(unitService.findById(10L)).thenReturn(Optional.of(unit(10L, "EA", false, null)));

    final ApiException exception = assertThrows(ApiException.class, () -> facade.create(request));

    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, exception.getMessageKey());
    verify(productService, never()).save(any(Product.class));
  }

  @Test
  void assignsCategoriesAndMapsAllSupportedAttributeTypes() {
    final List<ProductAttributeValueRequest> attributes =
        List.of(
            new ProductAttributeValueRequest(101L, 201L, null, null, null, null),
            new ProductAttributeValueRequest(102L, null, "Cotton", null, null, null),
            new ProductAttributeValueRequest(103L, null, null, new BigDecimal("2.5"), null, null),
            new ProductAttributeValueRequest(
                104L, null, null, null, java.time.LocalDate.parse("2026-01-05"), null),
            new ProductAttributeValueRequest(105L, null, null, null, null, true));
    final ProductRequest request = productRequest(null, null, List.of(301L), attributes);
    final ProductResponse response = response(100L);
    prepareCreate(request, unit(10L, "EA", true, null), response);
    final ProductCategory category =
        ProductCategory.builder().id(301L).code("TOOLS").name("Tools").build();
    when(categoryService.findByIdAndCompanyId(301L, 1L)).thenReturn(Optional.of(category));
    final Map<Long, ProductAttributeDefinition> definitions =
        Map.of(
            101L, definition(101L, ProductAttributeValueType.FIXED, false),
            102L, definition(102L, ProductAttributeValueType.TEXT, false),
            103L, definition(103L, ProductAttributeValueType.NUMBER, false),
            104L, definition(104L, ProductAttributeValueType.DATE, false),
            105L, definition(105L, ProductAttributeValueType.BOOLEAN, false));
    when(definitionService.findByIdAndCompanyId(any(Long.class), eq(1L)))
        .thenAnswer(call -> Optional.of(definitions.get(call.getArgument(0))));
    when(definitionService.findAllByCompanyId(1L)).thenReturn(List.copyOf(definitions.values()));
    when(optionService.findByIdAndDefinitionId(201L, 101L))
        .thenReturn(Optional.of(ProductAttributeOption.builder().id(201L).value("Steel").build()));

    facade.create(request);

    final ArgumentCaptor<ProductCategoryAssignment> assignmentCaptor =
        ArgumentCaptor.forClass(ProductCategoryAssignment.class);
    verify(assignmentService).save(assignmentCaptor.capture());
    assertEquals(category, assignmentCaptor.getValue().getCategory());
    final ArgumentCaptor<ProductAttributeValue> valueCaptor =
        ArgumentCaptor.forClass(ProductAttributeValue.class);
    verify(valueService, org.mockito.Mockito.times(5)).save(valueCaptor.capture());
    final List<ProductAttributeValue> values = valueCaptor.getAllValues();
    assertEquals("Steel", values.get(0).getOption().getValue());
    assertEquals("Cotton", values.get(1).getTextValue());
    assertEquals(new BigDecimal("2.5"), values.get(2).getNumberValue());
    assertEquals(java.time.LocalDate.parse("2026-01-05"), values.get(3).getDateValue());
    assertEquals(Boolean.TRUE, values.get(4).getBooleanValue());
  }

  @Test
  void rejectsDuplicateCategoryIds() {
    final ProductRequest duplicateCategories =
        productRequest(null, null, List.of(301L, 301L), List.of());
    stubProductCreation(duplicateCategories, unit(10L, "EA", true, null));
    final ApiException duplicate =
        assertThrows(ApiException.class, () -> facade.create(duplicateCategories));
    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, duplicate.getMessageKey());
    verify(categoryService, never()).findByIdAndCompanyId(301L, 1L);
    verify(categoryService, never()).findByIdAndCompanyId(301L, 1L);
  }

  @Test
  void rejectsOmittingARequiredAttribute() {
    final ProductRequest missingAttribute = productRequest(null, null, List.of(), List.of());
    stubProductCreation(missingAttribute, unit(10L, "EA", true, null));
    when(definitionService.findAllByCompanyId(1L))
        .thenReturn(List.of(definition(102L, ProductAttributeValueType.TEXT, true)));
    final ApiException required =
        assertThrows(ApiException.class, () -> facade.create(missingAttribute));
    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, required.getMessageKey());
  }

  @Test
  void preventsChangingProductUnitsAfterStockMovementsExist() {
    final Product existing = Product.builder().id(50L).unit(unit(10L, "EA", true, null)).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.findByIdAndCompanyId(50L, 1L)).thenReturn(Optional.of(existing));
    when(productService.existsByCompanyIdAndSkuAndIdNot(1L, "SKU-1", 50L)).thenReturn(false);
    when(unitService.findById(11L)).thenReturn(Optional.of(unit(11L, "BOX", true, null)));
    when(movementService.existsByProductId(50L)).thenReturn(true);

    final ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                facade.update(
                    50L,
                    productRequest(
                        null, new ProductRequest.Units(11L, null, null), List.of(), List.of())));

    assertEquals(ProductMessageKey.PRODUCT_UNIT_IN_USE, exception.getMessageKey());
    verify(productService, never()).save(existing);
  }

  @Test
  void preventsChangingSecondaryUnitAfterStockMovementsExist() {
    final Unit mainUnit = unit(10L, "BOX", true, null);
    final Unit currentSecondary = unit(11L, "PAIR", true, null);
    final Unit requestedSecondary = unit(12L, "EACH", true, null);
    final Product existing =
        Product.builder()
            .id(50L)
            .unit(mainUnit)
            .secondaryUnit(currentSecondary)
            .secondaryUnitsPerMainUnit(new BigDecimal("12"))
            .build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.findByIdAndCompanyId(50L, 1L)).thenReturn(Optional.of(existing));
    when(productService.existsByCompanyIdAndSkuAndIdNot(1L, "SKU-1", 50L)).thenReturn(false);
    when(unitService.findById(10L)).thenReturn(Optional.of(mainUnit));
    when(unitService.findById(12L)).thenReturn(Optional.of(requestedSecondary));
    when(movementService.existsByProductId(50L)).thenReturn(true);

    final ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                facade.update(
                    50L,
                    productRequest(
                        null,
                        new ProductRequest.Units(10L, 12L, new BigDecimal("12")),
                        List.of(),
                        List.of())));

    assertEquals(ProductMessageKey.PRODUCT_UNIT_IN_USE, exception.getMessageKey());
    verify(productService, never()).save(existing);
  }

  @Test
  void updatesProductWhenItsStockUnitConfigurationHasNotChanged() {
    final Unit mainUnit = unit(10L, "EA", true, null);
    final Unit formerSecondaryUnit = unit(11L, "BOX", true, null);
    final Currency formerCurrency = Currency.builder().id(9L).code("USD").build();
    final Product product =
        Product.builder()
            .id(50L)
            .unit(mainUnit)
            .secondaryUnit(formerSecondaryUnit)
            .secondaryUnitsPerMainUnit(new BigDecimal("12"))
            .currency(formerCurrency)
            .sku("OLD")
            .status(ProductStatus.ACTIVE)
            .build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.findByIdAndCompanyId(50L, 1L)).thenReturn(Optional.of(product));
    when(productService.existsByCompanyIdAndSkuAndIdNot(1L, "SKU-1", 50L)).thenReturn(false);
    when(unitService.findById(10L)).thenReturn(Optional.of(mainUnit));
    when(movementService.existsByProductId(50L)).thenReturn(false);
    when(productService.save(product)).thenReturn(product);
    when(assignmentService.findAllByProductId(50L)).thenReturn(List.of());
    when(valueService.findAllByProductId(50L)).thenReturn(List.of());
    when(stockBalanceService.findAllByProductId(50L)).thenReturn(List.of());
    final ProductResponse expected = response(50L);
    stubResponseMapping(expected);

    final ProductRequest request =
        productRequest(ProductStatus.DISCONTINUED, null, List.of(), List.of());
    final ProductResponse result = facade.update(50L, request);

    assertEquals(expected, result);
    assertEquals("SKU-1", product.getSku());
    assertEquals(ProductStatus.DISCONTINUED, product.getStatus());
    assertEquals(mainUnit, product.getUnit());
    assertNull(product.getSecondaryUnit());
    assertNull(product.getSecondaryUnitsPerMainUnit());
    assertNull(product.getCurrency());
    verify(productMapper).updateScalarFields(request, product);
    verify(assignmentService).deleteAllByProductId(50L);
    verify(valueService).deleteAllByProductId(50L);
    verify(productService).save(product);
  }

  @Test
  void listsEmptyPagesWithoutLoadingRelatedProductData() {
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.search(eq(1L), eq(false), any()))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 5), 0));

    final PageResponse<ProductResponse> response = facade.list(false, listRequest());

    assertTrue(response.content().isEmpty());
    assertEquals(2, response.page().number());
    assertFalse(response.page().first());
    assertTrue(response.page().last());
    verify(assignmentService, never()).findAllByProductIdIn(anyList());
    verify(valueService, never()).findAllByProductIdIn(anyList());
    verify(stockBalanceService, never()).findAllByProductIdIn(anyList());
  }

  @Test
  void listsProductsWithTheirGroupedCategoriesAttributesAndStock() {
    final Product first = Product.builder().id(50L).unit(unit(10L, "EA", true, null)).build();
    final Product second = Product.builder().id(51L).unit(unit(10L, "EA", true, null)).build();
    final List<Product> products = List.of(first, second);
    final ProductCategory category = ProductCategory.builder().id(301L).name("Tools").build();
    final ProductCategoryAssignment assignment =
        ProductCategoryAssignment.builder().product(first).category(category).build();
    final ProductAttributeValue value =
        ProductAttributeValue.builder().id(401L).product(first).build();
    final Warehouse warehouse = Warehouse.builder().id(3L).name("Main").build();
    final StockBalance balance =
        StockBalance.builder().product(first).warehouse(warehouse).quantity(BigDecimal.ONE).build();
    final ProductAttributeValueResponse attribute =
        new ProductAttributeValueResponse(
            5L,
            "COLOR",
            "Color",
            ProductAttributeValueType.TEXT,
            null,
            null,
            "Blue",
            null,
            null,
            null);
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.search(eq(1L), eq(false), any()))
        .thenReturn(new PageImpl<>(products, PageRequest.of(0, 10), 2));
    when(assignmentService.findAllByProductIdIn(List.of(50L, 51L))).thenReturn(List.of(assignment));
    when(valueService.findAllByProductIdIn(List.of(50L, 51L))).thenReturn(List.of(value));
    when(stockBalanceService.findAllByProductIdIn(List.of(50L, 51L))).thenReturn(List.of(balance));
    when(productMapper.toAttributeValueResponse(value)).thenReturn(attribute);
    when(productMapper.toStockQuantity(any(BigDecimal.class), any(Unit.class)))
        .thenAnswer(call -> new ProductStockResponse.Quantity(call.getArgument(0), null));
    when(productMapper.toStockBreakdown(any(), any()))
        .thenAnswer(
            call ->
                new ProductStockResponse.StockBreakdown(call.getArgument(0), call.getArgument(1)));
    when(productMapper.toStockResponse(any(), any(), any()))
        .thenAnswer(
            call -> new ProductStockResponse(3L, "Main", call.getArgument(1), call.getArgument(2)));
    when(productMapper.toProductResponse(
            any(Product.class), anyList(), anyList(), anyList(), nullable(BigDecimal.class)))
        .thenAnswer(call -> response(((Product) call.getArgument(0)).getId()));

    final PageResponse<ProductResponse> response = facade.list(false, listRequest());

    assertEquals(List.of(50L, 51L), response.content().stream().map(ProductResponse::id).toList());
    verify(productMapper)
        .toProductResponse(
            eq(first),
            eq(List.of(301L)),
            eq(List.of(attribute)),
            anyList(),
            nullable(BigDecimal.class));
    verify(productMapper)
        .toProductResponse(
            eq(second), eq(List.of()), eq(List.of()), eq(List.of()), nullable(BigDecimal.class));
  }

  @Test
  void hidesArchivedProductsUnlessExplicitlyIncluded() {
    final Product archived =
        Product.builder().id(50L).deletedAt(java.time.LocalDateTime.now()).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.findByIdAndCompanyId(50L, 1L)).thenReturn(Optional.of(archived));

    assertThrows(NotFoundException.class, () -> facade.get(50L, false));

    when(assignmentService.findAllByProductId(50L)).thenReturn(List.of());
    when(valueService.findAllByProductId(50L)).thenReturn(List.of());
    when(stockBalanceService.findAllByProductId(50L)).thenReturn(List.of());
    stubResponseMapping(response(50L));
    assertEquals(response(50L), facade.get(50L, true));
  }

  @Test
  void calculatesSecondaryUnitStockBreakdownAndPriceBeforeMappingResponse() {
    final Unit mainUnit = unit(10L, "BOX", true, null);
    final Unit secondaryUnit = unit(11L, "PAIR", true, null);
    final Product product =
        Product.builder()
            .id(50L)
            .sku("SKU-1")
            .name("Gloves")
            .unit(mainUnit)
            .secondaryUnit(secondaryUnit)
            .secondaryUnitsPerMainUnit(new BigDecimal("12"))
            .netPrice(new BigDecimal("120"))
            .build();
    final Warehouse warehouse = Warehouse.builder().id(3L).name("Main").build();
    final StockBalance balance =
        StockBalance.builder()
            .product(product)
            .warehouse(warehouse)
            .quantity(new BigDecimal("35"))
            .build();
    stubGetProduct(product, List.of(balance));
    when(productMapper.toStockQuantity(any(BigDecimal.class), any(Unit.class)))
        .thenAnswer(
            call ->
                new ProductStockResponse.Quantity(
                    call.getArgument(0), unitResponse(call.getArgument(1))));
    when(productMapper.toStockBreakdown(any(), any()))
        .thenAnswer(
            call ->
                new ProductStockResponse.StockBreakdown(call.getArgument(0), call.getArgument(1)));
    when(productMapper.toStockResponse(any(), any(), any()))
        .thenAnswer(
            call ->
                new ProductStockResponse(
                    warehouse.getId(),
                    warehouse.getName(),
                    call.getArgument(1),
                    call.getArgument(2)));

    facade.get(50L, false);

    final ArgumentCaptor<List<ProductStockResponse>> stockCaptor =
        ArgumentCaptor.forClass(List.class);
    final ArgumentCaptor<BigDecimal> secondaryPriceCaptor =
        ArgumentCaptor.forClass(BigDecimal.class);
    verify(productMapper)
        .toProductResponse(
            eq(product),
            eq(List.of()),
            eq(List.of()),
            stockCaptor.capture(),
            secondaryPriceCaptor.capture());
    assertEquals(new BigDecimal("10.00000000"), secondaryPriceCaptor.getValue());
    final ProductStockResponse stock = stockCaptor.getValue().getFirst();
    assertEquals(new BigDecimal("35"), stock.stockQuantity().amount());
    assertEquals(new BigDecimal("2"), stock.displayQuantity().fullMainUnits().amount());
    assertEquals(new BigDecimal("11"), stock.displayQuantity().secondaryUnitRemainder().amount());
    assertEquals("PAIR", stock.stockQuantity().unit().code());
    assertEquals("BOX", stock.displayQuantity().fullMainUnits().unit().code());
  }

  @Test
  void archivesAndRestoresProductsAndRejectsInvalidLifecycleTransitions() {
    final Product product = Product.builder().id(50L).deletedAt(null).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.findByIdAndCompanyId(50L, 1L)).thenReturn(Optional.of(product));

    facade.archive(50L);
    assertNotNull(product.getDeletedAt());
    verify(productService).save(product);
    assertThrows(ApiException.class, () -> facade.archive(50L));
    product.setDeletedAt(null);
    assertThrows(ApiException.class, () -> facade.restore(50L));

    product.setDeletedAt(java.time.LocalDateTime.now());
    when(productService.save(product)).thenReturn(product);
    stubSingleProductResponse(product);
    stubResponseMapping(response(50L));
    assertEquals(response(50L), facade.restore(50L));
    assertNull(product.getDeletedAt());
  }

  private ProductListRequest listRequest() {
    return new ProductListRequest(0, 10, null, null, null, null, null, null, null);
  }

  private void prepareCreate(
      final ProductRequest request, final Unit mainUnit, final ProductResponse response) {
    stubProductCreation(request, mainUnit);
    when(assignmentService.findAllByProductId(100L)).thenReturn(List.of());
    when(valueService.findAllByProductId(100L)).thenReturn(List.of());
    when(stockBalanceService.findAllByProductId(100L)).thenReturn(List.of());
    when(definitionService.findAllByCompanyId(1L)).thenReturn(List.of());
    stubResponseMapping(response);
  }

  private void stubProductCreation(final ProductRequest request, final Unit mainUnit) {
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.existsByCompanyIdAndSku(1L, "SKU-1")).thenReturn(false);
    when(unitService.findById(request.units().mainUnitId())).thenReturn(Optional.of(mainUnit));
    when(productService.save(any(Product.class)))
        .thenAnswer(
            call -> {
              final Product product = call.getArgument(0);
              product.setId(100L);
              return product;
            });
  }

  private void stubGetProduct(final Product product, final List<StockBalance> balances) {
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
    when(productService.findByIdAndCompanyId(product.getId(), 1L)).thenReturn(Optional.of(product));
    when(assignmentService.findAllByProductId(product.getId())).thenReturn(List.of());
    when(valueService.findAllByProductId(product.getId())).thenReturn(List.of());
    when(stockBalanceService.findAllByProductId(product.getId())).thenReturn(balances);
    stubResponseMapping(response(product.getId()));
  }

  private void stubSingleProductResponse(final Product product) {
    when(assignmentService.findAllByProductId(product.getId())).thenReturn(List.of());
    when(valueService.findAllByProductId(product.getId())).thenReturn(List.of());
    when(stockBalanceService.findAllByProductId(product.getId())).thenReturn(List.of());
  }

  private void stubResponseMapping(final ProductResponse response) {
    when(productMapper.toProductResponse(
            any(Product.class), anyList(), anyList(), anyList(), nullable(BigDecimal.class)))
        .thenReturn(response);
  }

  private ProductRequest productRequest(
      final ProductStatus status,
      final ProductRequest.Units units,
      final List<Long> categoryIds,
      final List<ProductAttributeValueRequest> attributes) {
    return new ProductRequest(
        " SKU-1 ",
        null,
        "Product",
        null,
        null,
        status,
        units == null ? new ProductRequest.Units(10L, null, null) : units,
        new ProductRequest.Pricing(null, new BigDecimal("10"), null, BigDecimal.ZERO),
        null,
        categoryIds,
        attributes);
  }

  private ProductResponse response(final Long productId) {
    return new ProductResponse(
        productId,
        "SKU-1",
        null,
        "Product",
        null,
        null,
        ProductStatus.ACTIVE,
        new ProductResponse.Units(null, null, null),
        new ProductResponse.Pricing(null, BigDecimal.TEN, null, null, BigDecimal.ZERO),
        new ProductResponse.Dimensions(null, null, null, null),
        List.of(),
        List.<ProductAttributeValueResponse>of(),
        List.of(),
        null,
        null,
        null);
  }

  private ProductAttributeDefinition definition(
      final Long id, final ProductAttributeValueType valueType, final boolean required) {
    return ProductAttributeDefinition.builder()
        .id(id)
        .code("ATTRIBUTE_" + id)
        .name("Attribute " + id)
        .valueType(valueType)
        .required(required)
        .build();
  }

  private Unit unit(
      final Long id, final String code, final boolean system, final Company unitCompany) {
    return Unit.builder()
        .id(id)
        .code(code)
        .name(code)
        .symbol(code.toLowerCase())
        .system(system)
        .company(unitCompany)
        .build();
  }

  private UnitResponse unitResponse(final Unit unit) {
    return new UnitResponse(
        unit.getId(), unit.getCode(), unit.getName(), unit.getSymbol(), unit.isSystem());
  }
}
