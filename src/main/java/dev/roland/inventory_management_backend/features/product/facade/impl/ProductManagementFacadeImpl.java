package dev.roland.inventory_management_backend.features.product.facade.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.dto.PageResponse;
import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.currency.service.CurrencyService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.dto.ProductAttributeValueRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductAttributeValueResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductListRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductStockResponse;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.product.facade.ProductManagementFacade;
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
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignmentId;
import dev.roland.inventory_management_backend.features.product_category_assignment.service.ProductCategoryAssignmentService;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.service.UnitService;
import lombok.RequiredArgsConstructor;

/** Implements product catalog operations. */
@Service
@RequiredArgsConstructor
public class ProductManagementFacadeImpl implements ProductManagementFacade {
  private final ProductService productService;
  private final ProductMapper productMapper;
  private final CompanyService companyService;
  private final UnitService unitService;
  private final CurrencyService currencyService;
  private final ProductCategoryService categoryService;
  private final ProductCategoryAssignmentService assignmentService;
  private final ProductAttributeDefinitionService definitionService;
  private final ProductAttributeOptionService optionService;
  private final ProductAttributeValueService valueService;
  private final StockMovementService movementService;
  private final StockBalanceService stockBalanceService;

  @Transactional
  @Override
  public PageResponse<ProductResponse> list(
      final boolean includeArchived, final ProductListRequest request) {
    final Company company = companyService.getCompanyOrCreateNew();
    final Page<Product> products = productService.search(company.getId(), includeArchived, request);
    final List<Long> productIds = products.getContent().stream().map(Product::getId).toList();
    final Page<ProductResponse> responsePage;
    if (productIds.isEmpty()) {
      responsePage = products.map(this::toResponse);
    } else {
      final Map<Long, List<ProductCategoryAssignment>> assignmentsByProduct =
          assignmentService.findAllByProductIdIn(productIds).stream()
              .collect(Collectors.groupingBy(assignment -> assignment.getProduct().getId()));
      final Map<Long, List<ProductAttributeValue>> valuesByProduct =
          valueService.findAllByProductIdIn(productIds).stream()
              .collect(Collectors.groupingBy(value -> value.getProduct().getId()));
      final Map<Long, List<StockBalance>> balancesByProduct =
          stockBalanceService.findAllByProductIdIn(productIds).stream()
              .collect(Collectors.groupingBy(balance -> balance.getProduct().getId()));
      responsePage =
          products.map(
              product ->
                  toResponse(
                      product,
                      assignmentsByProduct.getOrDefault(product.getId(), List.of()),
                      valuesByProduct.getOrDefault(product.getId(), List.of()),
                      balancesByProduct.getOrDefault(product.getId(), List.of())));
    }
    return PageResponse.from(responsePage);
  }

  @Transactional
  @Override
  public ProductResponse get(final Long id, final boolean includeArchived) {
    final Product product = findProduct(id);
    if (!includeArchived && product.getDeletedAt() != null) {
      throw new NotFoundException(NotFoundMessageKey.PRODUCT);
    }
    return toResponse(product);
  }

  @Transactional
  @Override
  public ProductResponse create(final ProductRequest request) {
    final Company company = companyService.getCompanyOrCreateNew();
    if (productService.existsByCompanyIdAndSku(company.getId(), request.sku().trim())) {
      throw new ApiException(ProductMessageKey.PRODUCT_SKU_ALREADY_EXISTS);
    }
    final Product product = new Product();
    product.setCompany(company);
    product.setStatus(ProductStatus.ACTIVE);
    apply(product, request, company);
    final Product saved = productService.save(product);
    replaceAssignments(saved, request, company);
    return toResponse(saved);
  }

  @Transactional
  @Override
  public ProductResponse update(final Long id, final ProductRequest request) {
    final Company company = companyService.getCompanyOrCreateNew();
    final Product product = findProduct(id);
    if (product.getDeletedAt() != null) {
      throw new ApiException(ProductMessageKey.PRODUCT_ALREADY_ARCHIVED);
    }
    if (productService.existsByCompanyIdAndSkuAndIdNot(company.getId(), request.sku().trim(), id)) {
      throw new ApiException(ProductMessageKey.PRODUCT_SKU_ALREADY_EXISTS);
    }
    final Unit requestedMain = findSelectableUnit(request.units().mainUnitId(), company);
    final Unit requestedSecondary =
        request.units().secondaryUnitId() == null
            ? null
            : findSelectableUnit(request.units().secondaryUnitId(), company);
    if (movementService.existsByProductId(id)
        && (!Objects.equals(idOf(product.getUnit()), idOf(requestedMain))
            || !Objects.equals(idOf(product.getSecondaryUnit()), idOf(requestedSecondary)))) {
      throw new ApiException(ProductMessageKey.PRODUCT_UNIT_IN_USE);
    }
    apply(product, request, company);
    final Product saved = productService.save(product);
    replaceAssignments(saved, request, company);
    return toResponse(saved);
  }

  @Transactional
  @Override
  public void archive(final Long id) {
    final Product product = findProduct(id);
    if (product.getDeletedAt() != null) {
      throw new ApiException(ProductMessageKey.PRODUCT_ALREADY_ARCHIVED);
    }
    product.setDeletedAt(LocalDateTime.now());
    productService.save(product);
  }

  @Transactional
  @Override
  public ProductResponse restore(final Long id) {
    final Product product = findProduct(id);
    if (product.getDeletedAt() == null) {
      throw new ApiException(ProductMessageKey.PRODUCT_NOT_ARCHIVED);
    }
    product.setDeletedAt(null);
    return toResponse(productService.save(product));
  }

  private void apply(final Product product, final ProductRequest request, final Company company) {
    productMapper.updateScalarFields(request, product);
    product.setSku(request.sku().trim());
    if (request.status() != null) {
      if (request.status() == ProductStatus.ARCHIVED) {
        throw new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA);
      }
      product.setStatus(request.status());
    }
    product.setUnit(findSelectableUnit(request.units().mainUnitId(), company));
    product.setSecondaryUnit(
        request.units().secondaryUnitId() == null
            ? null
            : findSelectableUnit(request.units().secondaryUnitId(), company));
    product.setSecondaryUnitsPerMainUnit(request.units().secondaryUnitsPerMainUnit());
    product.setCurrency(
        request.pricing().currencyId() == null
            ? null
            : currencyService
                .findById(request.pricing().currencyId())
                .orElseThrow(() -> new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA)));
  }

  private void replaceAssignments(
      final Product product, final ProductRequest request, final Company company) {
    assignmentService.deleteAllByProductId(product.getId());
    valueService.deleteAllByProductId(product.getId());

    final Set<Long> categoryIds =
        new HashSet<>(
            request.categoryIds() == null
                ? List.of()
                : Objects.requireNonNull(request.categoryIds()));
    if (request.categoryIds() != null
        && categoryIds.size() != Objects.requireNonNull(request.categoryIds()).size()) {
      throw new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA);
    }
    for (Long categoryId : categoryIds) {
      final ProductCategory category =
          categoryService
              .findByIdAndCompanyId(categoryId, company.getId())
              .orElseThrow(() -> new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA));

      assignmentService.save(
          ProductCategoryAssignment.builder()
              .id(new ProductCategoryAssignmentId(product.getId(), category.getId()))
              .product(product)
              .category(category)
              .build());
    }

    final List<ProductAttributeValueRequest> attributes =
        request.attributes() == null ? List.of() : request.attributes();
    final Set<Long> providedIds = new HashSet<>();
    assert attributes != null;
    for (ProductAttributeValueRequest input : attributes) {
      if (input == null || input.definitionId() == null || !providedIds.add(input.definitionId())) {
        throw new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA);
      }
      final ProductAttributeDefinition definition =
          definitionService
              .findByIdAndCompanyId(input.definitionId(), company.getId())
              .orElseThrow(() -> new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA));
      final ProductAttributeValue value = makeAttributeValue(product, definition, input);
      valueService.save(value);
    }
    for (ProductAttributeDefinition definition :
        definitionService.findAllByCompanyId(company.getId())) {
      if (definition.isRequired() && !providedIds.contains(definition.getId())) {
        throw new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA);
      }
    }
  }

  private ProductAttributeValue makeAttributeValue(
      final Product product,
      final ProductAttributeDefinition definition,
      final ProductAttributeValueRequest input) {
    final ProductAttributeValue value =
        ProductAttributeValue.builder().product(product).definition(definition).build();
    if (definition.getValueType() == ProductAttributeValueType.FIXED && input.optionId() != null) {
      final ProductAttributeOption option =
          optionService
              .findByIdAndDefinitionId(input.optionId(), definition.getId())
              .orElseThrow(() -> new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA));
      value.setOption(option);
    } else if (definition.getValueType() == ProductAttributeValueType.TEXT
        && input.textValue() != null) {
      value.setTextValue(input.textValue());
    } else if (definition.getValueType() == ProductAttributeValueType.NUMBER
        && input.numberValue() != null) {
      value.setNumberValue(input.numberValue());
    } else if (definition.getValueType() == ProductAttributeValueType.DATE
        && input.dateValue() != null) {
      value.setDateValue(input.dateValue());
    } else if (definition.getValueType() == ProductAttributeValueType.BOOLEAN
        && input.booleanValue() != null) {
      value.setBooleanValue(input.booleanValue());
    } else {
      throw new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA);
    }
    return value;
  }

  private Unit findSelectableUnit(final Long id, final Company company) {
    final Unit unit =
        unitService
            .findById(id)
            .orElseThrow(() -> new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA));
    if (!unit.isSystem()
        && (unit.getCompany() == null || !unit.getCompany().getId().equals(company.getId()))) {
      throw new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA);
    }
    return unit;
  }

  private Product findProduct(final Long id) {
    return productService
        .findByIdAndCompanyId(id, companyService.getCompanyOrCreateNew().getId())
        .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PRODUCT));
  }

  private ProductResponse toResponse(final Product product) {
    return toResponse(
        product,
        assignmentService.findAllByProductId(product.getId()),
        valueService.findAllByProductId(product.getId()),
        stockBalanceService.findAllByProductId(product.getId()));
  }

  private ProductResponse toResponse(
      final Product product,
      final List<ProductCategoryAssignment> assignments,
      final List<ProductAttributeValue> attributeValues,
      final List<StockBalance> stockBalances) {
    final List<Long> categoryIds = assignments.stream().map(a -> a.getCategory().getId()).toList();
    final List<ProductAttributeValueResponse> attributes =
        attributeValues.stream().map(productMapper::toAttributeValueResponse).toList();
    final Unit secondary = product.getSecondaryUnit();
    final List<ProductStockResponse> stockByWarehouse =
        stockBalances.stream().map(balance -> stockResponse(product, balance)).toList();
    return productMapper.toProductResponse(
        product, categoryIds, attributes, stockByWarehouse, secondaryNetPrice(product, secondary));
  }

  private BigDecimal secondaryNetPrice(final Product product, final Unit secondaryUnit) {
    return secondaryUnit == null
        ? null
        : product
            .getNetPrice()
            .divide(product.getSecondaryUnitsPerMainUnit(), 8, RoundingMode.HALF_UP);
  }

  private ProductStockResponse stockResponse(final Product product, final StockBalance balance) {
    final BigDecimal quantity =
        balance.getQuantity() == null ? BigDecimal.ZERO : balance.getQuantity();
    final Unit secondaryUnit = product.getSecondaryUnit();
    final ProductStockResponse.Quantity stockQuantity =
        productMapper.toStockQuantity(
            quantity, secondaryUnit == null ? product.getUnit() : secondaryUnit);
    final ProductStockResponse.Quantity fullMainUnits;
    final ProductStockResponse.Quantity secondaryUnitRemainder;
    if (secondaryUnit == null) {
      fullMainUnits = stockQuantity;
      secondaryUnitRemainder = null;
    } else {
      final BigDecimal conversion = product.getSecondaryUnitsPerMainUnit();
      fullMainUnits =
          productMapper.toStockQuantity(
              quantity.divideToIntegralValue(conversion), product.getUnit());
      secondaryUnitRemainder =
          productMapper.toStockQuantity(quantity.remainder(conversion), secondaryUnit);
    }
    final ProductStockResponse.StockBreakdown displayQuantity =
        productMapper.toStockBreakdown(fullMainUnits, secondaryUnitRemainder);
    return productMapper.toStockResponse(balance, stockQuantity, displayQuantity);
  }

  private Long idOf(final Unit unit) {
    return unit == null ? null : unit.getId();
  }
}
