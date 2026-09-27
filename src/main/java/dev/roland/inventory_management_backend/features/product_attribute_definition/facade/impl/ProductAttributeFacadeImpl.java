package dev.roland.inventory_management_backend.features.product_attribute_definition.facade.impl;

import java.util.List;
import java.util.Objects;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionResponse;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionResponse;
import dev.roland.inventory_management_backend.features.product.mapper.ProductSettingsMapper;
import dev.roland.inventory_management_backend.features.product.message.ProductMessageKey;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;
import dev.roland.inventory_management_backend.features.product_attribute_definition.facade.ProductAttributeFacade;
import dev.roland.inventory_management_backend.features.product_attribute_definition.service.ProductAttributeDefinitionService;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;
import dev.roland.inventory_management_backend.features.product_attribute_option.service.ProductAttributeOptionService;
import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;
import dev.roland.inventory_management_backend.features.product_attribute_value.service.ProductAttributeValueService;
import lombok.RequiredArgsConstructor;

/** Implements product attribute definition and option operations. */
@Service
@RequiredArgsConstructor
public class ProductAttributeFacadeImpl implements ProductAttributeFacade {
  private final ProductAttributeDefinitionService definitionService;
  private final ProductAttributeOptionService optionService;
  private final ProductAttributeValueService valueService;
  private final CompanyService companyService;
  private final ProductSettingsMapper mapper;

  @Override
  @Transactional
  public List<AttributeDefinitionResponse> list() {
    return definitionService.findAllByCompanyId(companyId()).stream()
        .map(this::definitionResponse)
        .toList();
  }

  @Override
  @Transactional
  public AttributeDefinitionResponse create(final AttributeDefinitionRequest request) {
    final var company = companyService.getCompanyOrCreateNew();
    final String code = request.code().trim();
    if (definitionService.existsByCompanyIdAndCode(company.getId(), code)) {
      throw invalid();
    }
    final ProductAttributeDefinition definition =
        ProductAttributeDefinition.builder()
            .company(company)
            .code(code)
            .name(request.name().trim())
            .valueType(request.valueType())
            .required(request.required())
            .build();
    return definitionResponse(definitionService.save(definition));
  }

  @Override
  @Transactional
  public AttributeDefinitionResponse update(
      final Long id, final AttributeDefinitionRequest request) {
    final ProductAttributeDefinition definition = findDefinition(id);
    final ProductAttributeValueType valueType = request.valueType();
    if (definition.getValueType() != valueType
        && (optionService.existsByDefinitionId(id) || valueService.existsByDefinitionId(id))) {
      throw invalid();
    }
    final String code = request.code().trim();
    if (!definition.getCode().equals(code)
        && definitionService.existsByCompanyIdAndCode(companyId(), code)) {
      throw invalid();
    }
    definition.setCode(code);
    definition.setName(request.name().trim());
    definition.setValueType(valueType);
    definition.setRequired(request.required());
    return definitionResponse(definitionService.save(definition));
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    definitionService.delete(findDefinition(id));
  }

  @Override
  @Transactional
  public AttributeOptionResponse createOption(
      final Long definitionId, final AttributeOptionRequest request) {
    final ProductAttributeDefinition definition = findDefinition(definitionId);
    final String value = request.value().trim();
    if (definition.getValueType() != ProductAttributeValueType.FIXED
        || optionService.existsByDefinitionIdAndValue(definitionId, value)) {
      throw invalid();
    }
    final ProductAttributeOption option =
        ProductAttributeOption.builder()
            .definition(definition)
            .value(value)
            .sortOrder(Objects.requireNonNullElse(request.sortOrder(), Integer.valueOf(0)))
            .build();
    return mapper.toAttributeOptionResponse(optionService.save(option));
  }

  @Override
  @Transactional
  public AttributeOptionResponse updateOption(
      final Long definitionId, final Long optionId, final AttributeOptionRequest request) {
    final ProductAttributeDefinition definition = findDefinition(definitionId);
    final ProductAttributeOption option =
        optionService
            .findByIdAndDefinitionId(optionId, definition.getId())
            .orElseThrow(this::invalid);
    final String value = request.value().trim();
    if (optionService.existsByDefinitionIdAndValueAndIdNot(definitionId, value, optionId)) {
      throw invalid();
    }
    option.setValue(value);
    option.setSortOrder(Objects.requireNonNullElse(request.sortOrder(), Integer.valueOf(0)));
    return mapper.toAttributeOptionResponse(optionService.save(option));
  }

  @Override
  @Transactional
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

  private ProductAttributeDefinition findDefinition(final Long id) {
    return definitionService.findByIdAndCompanyId(id, companyId()).orElseThrow(this::invalid);
  }

  private Long companyId() {
    return companyService.getCompanyOrCreateNew().getId();
  }

  private ApiException invalid() {
    return new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA);
  }

  private AttributeDefinitionResponse definitionResponse(
      final ProductAttributeDefinition definition) {
    return mapper.toAttributeDefinitionResponse(
        definition, optionService.findAllByDefinitionId(definition.getId()));
  }
}
