package dev.roland.inventory_management_backend.features.product_attribute_definition.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionRequest;
import dev.roland.inventory_management_backend.features.product.mapper.ProductSettingsMapperImpl;
import dev.roland.inventory_management_backend.features.product.message.ProductMessageKey;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;
import dev.roland.inventory_management_backend.features.product_attribute_definition.service.ProductAttributeDefinitionService;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;
import dev.roland.inventory_management_backend.features.product_attribute_option.service.ProductAttributeOptionService;
import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;
import dev.roland.inventory_management_backend.features.product_attribute_value.service.ProductAttributeValueService;

@ExtendWith(MockitoExtension.class)
class ProductAttributeFacadeImplTest {

  @Mock private ProductAttributeDefinitionService definitionService;
  @Mock private ProductAttributeOptionService optionService;
  @Mock private ProductAttributeValueService valueService;
  @Mock private CompanyService companyService;

  private ProductAttributeFacadeImpl attributeFacade;
  private Company company;

  @BeforeEach
  void setUp() {
    attributeFacade =
        new ProductAttributeFacadeImpl(
            definitionService,
            optionService,
            valueService,
            companyService,
            new ProductSettingsMapperImpl());
    company = Company.builder().id(1L).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
  }

  @Test
  void listsDefinitionsWithOptions() {
    final ProductAttributeDefinition definition =
        definition(30L, "COLOR", ProductAttributeValueType.FIXED, true);
    when(definitionService.findAllByCompanyId(1L)).thenReturn(List.of(definition));
    when(optionService.findAllByDefinitionId(30L))
        .thenReturn(
            List.of(ProductAttributeOption.builder().id(40L).value("Blue").sortOrder(0).build()));
    final var response = attributeFacade.list().getFirst();
    assertEquals("COLOR", response.code());
    assertEquals("Blue", response.options().getFirst().value());
  }

  @Test
  void createsAndUpdatesDefinitionsAndBlocksChangingUsedValueType() {
    when(definitionService.save(any(ProductAttributeDefinition.class)))
        .thenAnswer(call -> call.getArgument(0));
    final var created =
        attributeFacade.create(
            new AttributeDefinitionRequest(
                " COLOR ", " Color ", ProductAttributeValueType.TEXT, true));
    assertEquals("COLOR", created.code());
    assertTrue(created.required());

    final ProductAttributeDefinition existing =
        definition(30L, "COLOR", ProductAttributeValueType.TEXT, true);
    when(definitionService.findByIdAndCompanyId(30L, 1L)).thenReturn(Optional.of(existing));
    final var updated =
        attributeFacade.update(
            30L,
            new AttributeDefinitionRequest(
                "SHADE", "Shade", ProductAttributeValueType.NUMBER, false));
    assertEquals("SHADE", updated.code());
    assertEquals("Shade", updated.name());
    assertEquals(ProductAttributeValueType.NUMBER, updated.valueType());
    assertFalse(updated.required());

    when(optionService.existsByDefinitionId(30L)).thenReturn(true);
    final ApiException inUse =
        assertThrows(
            ApiException.class,
            () ->
                attributeFacade.update(
                    30L,
                    new AttributeDefinitionRequest(
                        "COLOR", "Shade", ProductAttributeValueType.FIXED, false)));
    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, inUse.getMessageKey());
  }

  @Test
  void allowsUpdatingDefinitionWithItsCurrentCodeEvenWhenThatCodeExists() {
    final ProductAttributeDefinition definition =
        definition(30L, "COLOR", ProductAttributeValueType.TEXT, false);
    when(definitionService.findByIdAndCompanyId(30L, 1L)).thenReturn(Optional.of(definition));
    lenient().when(definitionService.existsByCompanyIdAndCode(1L, "COLOR")).thenReturn(true);
    when(definitionService.save(definition)).thenReturn(definition);

    final var updated =
        attributeFacade.update(
            30L,
            new AttributeDefinitionRequest(
                "COLOR", "Colour", ProductAttributeValueType.TEXT, true));

    assertEquals("COLOR", updated.code());
    assertEquals("Colour", updated.name());
    assertTrue(updated.required());
  }

  @Test
  void rejectsDuplicateDefinitionAndDeletesDefinition() {
    when(definitionService.existsByCompanyIdAndCode(1L, "COLOR")).thenReturn(true);
    assertThrows(
        ApiException.class,
        () ->
            attributeFacade.create(
                new AttributeDefinitionRequest(
                    " COLOR ", "Color", ProductAttributeValueType.TEXT, false)));

    final ProductAttributeDefinition definition =
        definition(30L, "COLOR", ProductAttributeValueType.TEXT, false);
    when(definitionService.findByIdAndCompanyId(30L, 1L)).thenReturn(Optional.of(definition));
    attributeFacade.delete(30L);
    verify(definitionService).delete(definition);
  }

  @Test
  void createsUpdatesAndDeletesFixedOptions() {
    final ProductAttributeDefinition definition =
        definition(30L, "COLOR", ProductAttributeValueType.FIXED, false);
    when(definitionService.findByIdAndCompanyId(30L, 1L)).thenReturn(Optional.of(definition));
    when(optionService.save(any(ProductAttributeOption.class)))
        .thenAnswer(call -> call.getArgument(0));

    final var created =
        attributeFacade.createOption(30L, new AttributeOptionRequest(" Blue ", null));
    assertEquals("Blue", created.value());
    assertEquals(0, created.sortOrder());

    final ProductAttributeOption option =
        ProductAttributeOption.builder()
            .id(40L)
            .definition(definition)
            .value("Blue")
            .sortOrder(0)
            .build();
    when(optionService.findByIdAndDefinitionId(40L, 30L)).thenReturn(Optional.of(option));
    final var updated =
        attributeFacade.updateOption(30L, 40L, new AttributeOptionRequest(" Navy ", 2));
    assertEquals("Navy", updated.value());
    assertEquals(2, updated.sortOrder());

    attributeFacade.deleteOption(30L, 40L);
    verify(optionService).delete(option);
  }

  @Test
  void rejectsNonFixedDefinitionsDuplicateOptionsAndDeletingUsedOptions() {
    final ProductAttributeDefinition textDefinition =
        definition(30L, "NOT_FIXED", ProductAttributeValueType.TEXT, false);
    when(definitionService.findByIdAndCompanyId(30L, 1L)).thenReturn(Optional.of(textDefinition));
    assertThrows(
        ApiException.class,
        () -> attributeFacade.createOption(30L, new AttributeOptionRequest("Blue", 0)));

    final ProductAttributeDefinition fixedDefinition =
        definition(31L, "COLOR", ProductAttributeValueType.FIXED, false);
    when(definitionService.findByIdAndCompanyId(31L, 1L)).thenReturn(Optional.of(fixedDefinition));
    when(optionService.existsByDefinitionIdAndValue(31L, "Blue")).thenReturn(true);
    assertThrows(
        ApiException.class,
        () -> attributeFacade.createOption(31L, new AttributeOptionRequest("Blue", 0)));

    final ProductAttributeOption option =
        ProductAttributeOption.builder().id(41L).definition(fixedDefinition).value("Blue").build();
    when(optionService.findByIdAndDefinitionId(41L, 31L)).thenReturn(Optional.of(option));
    when(valueService.existsByOptionId(41L)).thenReturn(true);
    assertThrows(ApiException.class, () -> attributeFacade.deleteOption(31L, 41L));
    verify(optionService, never()).delete(option);
  }

  private ProductAttributeDefinition definition(
      final Long id,
      final String code,
      final ProductAttributeValueType valueType,
      final boolean required) {
    return ProductAttributeDefinition.builder()
        .id(id)
        .company(company)
        .code(code)
        .name(code)
        .valueType(valueType)
        .required(required)
        .build();
  }
}
