package dev.roland.inventory_management_backend.features.product.facade.impl;

import java.util.List;
import java.util.Objects;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionResponse;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionResponse;
import dev.roland.inventory_management_backend.features.product.dto.CategoryRequest;
import dev.roland.inventory_management_backend.features.product.dto.CategoryResponse;
import dev.roland.inventory_management_backend.features.product.dto.UnitRequest;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.product.facade.ProductSettingsFacade;
import dev.roland.inventory_management_backend.features.product.mapper.ProductSettingsMapper;
import dev.roland.inventory_management_backend.features.product.message.ProductMessageKey;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;
import dev.roland.inventory_management_backend.features.product_attribute_definition.service.ProductAttributeDefinitionService;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;
import dev.roland.inventory_management_backend.features.product_attribute_option.service.ProductAttributeOptionService;
import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;
import dev.roland.inventory_management_backend.features.product_attribute_value.service.ProductAttributeValueService;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;
import dev.roland.inventory_management_backend.features.product_category.service.ProductCategoryService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.service.UnitService;
import lombok.RequiredArgsConstructor;

/** Implements company-scoped product catalog settings. */
@Service
@RequiredArgsConstructor
public class ProductSettingsFacadeImpl implements ProductSettingsFacade {
  private final UnitService unitService;
  private final ProductService productService;
  private final DocumentLineService documentLineService;
  private final ProductCategoryService categoryService;
  private final ProductAttributeDefinitionService definitionService;
  private final ProductAttributeOptionService optionService;
  private final ProductAttributeValueService valueService;
  private final CompanyService companyService;
  private final ProductSettingsMapper productSettingsMapper;

  @Override
  @Transactional
  public List<UnitResponse> listUnits() {
    return unitService.findSelectableUnits(company().getId()).stream()
        .map(productSettingsMapper::toUnitResponse)
        .toList();
  }

  @Transactional
  @Override
  public UnitResponse createUnit(final UnitRequest request) {
    if (unitService.existsByCompanyIdAndCode(company().getId(), request.code().trim())) {
      throw invalid();
    }
    final Unit unit =
        Unit.builder()
            .code(request.code().trim())
            .name(request.name().trim())
            .symbol(request.symbol().trim())
            .system(false)
            .company(company())
            .build();
    return productSettingsMapper.toUnitResponse(unitService.save(unit));
  }

  @Transactional
  @Override
  public UnitResponse updateUnit(final Long id, final UnitRequest request) {
    final Unit unit = findCompanyUnit(id);
    if (!unit.getCode().equals(request.code().trim())
        && unitService.existsByCompanyIdAndCode(company().getId(), request.code().trim())) {
      throw invalid();
    }
    unit.setCode(request.code().trim());
    unit.setName(request.name().trim());
    unit.setSymbol(request.symbol().trim());
    return productSettingsMapper.toUnitResponse(unitService.save(unit));
  }

  @Transactional
  @Override
  public void deleteUnit(final Long id) {
    final Unit unit = findCompanyUnit(id);
    if (productService.existsUsingUnit(id) || documentLineService.existsByUnitSnapshotId(id)) {
      throw new ApiException(ProductMessageKey.PRODUCT_UNIT_IN_USE);
    }
    unitService.delete(unit);
  }

  @Override
  @Transactional
  public List<CategoryResponse> listCategories() {
    return categoryService.findAllByCompanyId(company().getId()).stream()
        .map(productSettingsMapper::toCategoryResponse)
        .toList();
  }

  @Transactional
  @Override
  public CategoryResponse createCategory(final CategoryRequest request) {
    if (categoryService.existsByCompanyIdAndCode(company().getId(), request.code().trim())) {
      throw invalid();
    }
    final ProductCategory category =
        ProductCategory.builder()
            .company(company())
            .parent(findParent(request.parentId()))
            .code(request.code().trim())
            .name(request.name().trim())
            .description(request.description())
            .sortOrder(Objects.requireNonNullElse(request.sortOrder(), Integer.valueOf(0)))
            .build();
    return productSettingsMapper.toCategoryResponse(categoryService.save(category));
  }

  @Transactional
  @Override
  public CategoryResponse updateCategory(final Long id, final CategoryRequest request) {
    final ProductCategory category = findCategory(id);
    if (!category.getCode().equals(request.code().trim())
        && categoryService.existsByCompanyIdAndCode(company().getId(), request.code().trim())) {
      throw invalid();
    }
    final ProductCategory parent = findParent(request.parentId());
    if (parent != null && parent.getId().equals(id)) {
      throw invalid();
    }
    ProductCategory ancestor = parent;
    while (ancestor != null) {
      if (ancestor.getId().equals(id)) {
        throw invalid();
      }
      ancestor = ancestor.getParent();
    }
    category.setParent(parent);
    category.setCode(request.code().trim());
    category.setName(request.name().trim());
    category.setDescription(request.description());
    category.setSortOrder(Objects.requireNonNullElse(request.sortOrder(), Integer.valueOf(0)));
    return productSettingsMapper.toCategoryResponse(categoryService.save(category));
  }

  @Transactional
  @Override
  public void deleteCategory(final Long id) {
    categoryService.delete(findCategory(id));
  }

  @Override
  @Transactional
  public List<AttributeDefinitionResponse> listDefinitions() {
    return definitionService.findAllByCompanyId(company().getId()).stream()
        .map(this::definitionResponse)
        .toList();
  }

  @Transactional
  @Override
  public AttributeDefinitionResponse createDefinition(final AttributeDefinitionRequest request) {
    final ProductAttributeValueType valueType = request.valueType();
    if (definitionService.existsByCompanyIdAndCode(company().getId(), request.code().trim())) {
      throw invalid();
    }
    final ProductAttributeDefinition definition =
        ProductAttributeDefinition.builder()
            .company(company())
            .code(request.code().trim())
            .name(request.name().trim())
            .valueType(valueType)
            .required(request.required())
            .build();
    return definitionResponse(definitionService.save(definition));
  }

  @Transactional
  @Override
  public AttributeDefinitionResponse updateDefinition(
      final Long id, final AttributeDefinitionRequest request) {
    final ProductAttributeDefinition definition = findDefinition(id);
    final ProductAttributeValueType valueType = request.valueType();
    if (definition.getValueType() != valueType
        && (optionService.existsByDefinitionId(id) || valueService.existsByDefinitionId(id))) {
      throw invalid();
    }
    if (!definition.getCode().equals(request.code().trim())
        && definitionService.existsByCompanyIdAndCode(company().getId(), request.code().trim())) {
      throw invalid();
    }
    definition.setCode(request.code().trim());
    definition.setName(request.name().trim());
    definition.setValueType(valueType);
    definition.setRequired(request.required());
    return definitionResponse(definitionService.save(definition));
  }

  @Transactional
  @Override
  public void deleteDefinition(final Long id) {
    definitionService.delete(findDefinition(id));
  }

  @Transactional
  @Override
  public AttributeOptionResponse createOption(
      final Long definitionId, final AttributeOptionRequest request) {
    final ProductAttributeDefinition definition = findDefinition(definitionId);
    if (definition.getValueType() != ProductAttributeValueType.FIXED) {
      throw invalid();
    }
    if (optionService.existsByDefinitionIdAndValue(definitionId, request.value().trim())) {
      throw invalid();
    }
    final ProductAttributeOption option =
        ProductAttributeOption.builder()
            .definition(definition)
            .value(request.value().trim())
            .sortOrder(Objects.requireNonNullElse(request.sortOrder(), Integer.valueOf(0)))
            .build();
    return productSettingsMapper.toAttributeOptionResponse(optionService.save(option));
  }

  @Transactional
  @Override
  public AttributeOptionResponse updateOption(
      final Long definitionId, final Long optionId, final AttributeOptionRequest request) {
    final ProductAttributeDefinition definition = findDefinition(definitionId);
    final ProductAttributeOption option =
        optionService
            .findByIdAndDefinitionId(optionId, definition.getId())
            .orElseThrow(this::invalid);
    if (optionService.existsByDefinitionIdAndValueAndIdNot(
        definitionId, request.value().trim(), optionId)) {
      throw invalid();
    }
    option.setValue(request.value().trim());
    option.setSortOrder(Objects.requireNonNullElse(request.sortOrder(), Integer.valueOf(0)));
    return productSettingsMapper.toAttributeOptionResponse(optionService.save(option));
  }

  @Transactional
  @Override
  public void deleteOption(final Long definitionId, final Long optionId) {
    final ProductAttributeDefinition definition = findDefinition(definitionId);
    final ProductAttributeOption option =
        optionService
            .findByIdAndDefinitionId(optionId, definition.getId())
            .orElseThrow(this::invalid);
    if (valueService.existsByOptionId(optionId)) {
      throw invalid();
    }
    optionService.delete(option);
  }

  private Company company() {
    return companyService.getCompanyOrCreateNew();
  }

  private Unit findCompanyUnit(final Long id) {
    final Unit unit = unitService.findById(id).orElseThrow(this::invalid);
    if (unit.isSystem()
        || unit.getCompany() == null
        || !unit.getCompany().getId().equals(company().getId())) {
      throw invalid();
    }
    return unit;
  }

  private ProductCategory findCategory(final Long id) {
    return categoryService.findByIdAndCompanyId(id, company().getId()).orElseThrow(this::invalid);
  }

  private ProductCategory findParent(final Long parentId) {
    return parentId == null ? null : findCategory(parentId);
  }

  private ProductAttributeDefinition findDefinition(final Long id) {
    return definitionService.findByIdAndCompanyId(id, company().getId()).orElseThrow(this::invalid);
  }

  private ApiException invalid() {
    return new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA);
  }

  private AttributeDefinitionResponse definitionResponse(
      final ProductAttributeDefinition definition) {
    return productSettingsMapper.toAttributeDefinitionResponse(
        definition, optionService.findAllByDefinitionId(definition.getId()));
  }
}
