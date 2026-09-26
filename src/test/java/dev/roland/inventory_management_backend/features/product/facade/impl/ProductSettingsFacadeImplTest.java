package dev.roland.inventory_management_backend.features.product.facade.impl;

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
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionRequest;
import dev.roland.inventory_management_backend.features.product.dto.CategoryRequest;
import dev.roland.inventory_management_backend.features.product.dto.UnitRequest;
import dev.roland.inventory_management_backend.features.product.mapper.ProductSettingsMapperImpl;
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

@ExtendWith(MockitoExtension.class)
class ProductSettingsFacadeImplTest {

  @Mock private UnitService unitService;
  @Mock private ProductService productService;
  @Mock private DocumentLineService documentLineService;
  @Mock private ProductCategoryService categoryService;
  @Mock private ProductAttributeDefinitionService definitionService;
  @Mock private ProductAttributeOptionService optionService;
  @Mock private ProductAttributeValueService valueService;
  @Mock private CompanyService companyService;

  private ProductSettingsFacadeImpl facade;
  private Company company;

  @BeforeEach
  void setUp() {
    facade =
        new ProductSettingsFacadeImpl(
            unitService,
            productService,
            documentLineService,
            categoryService,
            definitionService,
            optionService,
            valueService,
            companyService,
            new ProductSettingsMapperImpl());
    company = Company.builder().id(1L).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
  }

  @Test
  void listsUnitsCategoriesAndDefinitionsWithOptions() {
    final Unit unit = unit(10L, "KG", "Kilogram", "kg", false);
    final ProductCategory category =
        ProductCategory.builder().id(20L).code("TOOLS").name("Tools").build();
    final ProductAttributeDefinition definition =
        definition(30L, "COLOR", ProductAttributeValueType.FIXED, true);
    final ProductAttributeOption option =
        ProductAttributeOption.builder().id(40L).value("Blue").sortOrder(0).build();
    when(unitService.findSelectableUnits(1L)).thenReturn(List.of(unit));
    when(categoryService.findAllByCompanyId(1L)).thenReturn(List.of(category));
    when(definitionService.findAllByCompanyId(1L)).thenReturn(List.of(definition));
    when(optionService.findAllByDefinitionId(30L)).thenReturn(List.of(option));

    assertEquals("KG", facade.listUnits().getFirst().code());
    assertEquals(20L, facade.listCategories().getFirst().id());
    final var definitionResponse = facade.listDefinitions().getFirst();
    assertEquals("COLOR", definitionResponse.code());
    assertEquals("Blue", definitionResponse.options().getFirst().value());
  }

  @Test
  void createsAndUpdatesCompanyUnitWithTrimmedValues() {
    when(unitService.save(any(Unit.class))).thenAnswer(call -> call.getArgument(0));
    final var created = facade.createUnit(new UnitRequest(" KG ", " Kilogram ", " kg "));

    assertEquals("KG", created.code());
    assertEquals("Kilogram", created.name());
    assertEquals("kg", created.symbol());

    final Unit existing = unit(10L, "G", "Gram", "g", false);
    existing.setCompany(company);
    when(unitService.findById(10L)).thenReturn(Optional.of(existing));
    final var updated = facade.updateUnit(10L, new UnitRequest(" GR ", " Grams ", " gr "));

    assertEquals("GR", updated.code());
    assertEquals("Grams", updated.name());
    assertEquals("gr", updated.symbol());
  }

  @Test
  void allowsUpdatingAUnitWithoutChangingItsExistingCode() {
    final Unit existing = unit(10L, "KG", "Kilogram", "kg", false);
    existing.setCompany(company);
    when(unitService.findById(10L)).thenReturn(Optional.of(existing));
    when(unitService.save(existing)).thenReturn(existing);
    lenient().when(unitService.existsByCompanyIdAndCode(1L, "KG")).thenReturn(true);

    final var updated = facade.updateUnit(10L, new UnitRequest(" KG ", "Kilograms ", "kgs"));

    assertEquals("KG", updated.code());
    assertEquals("Kilograms", updated.name());
    assertEquals("kgs", updated.symbol());
  }

  @Test
  void rejectsDuplicateUnitCodeAndUnitDeletionWhileInUse() {
    when(unitService.existsByCompanyIdAndCode(1L, "KG")).thenReturn(true);
    final ApiException duplicate =
        assertThrows(
            ApiException.class, () -> facade.createUnit(new UnitRequest(" KG ", "Kilogram", "kg")));
    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, duplicate.getMessageKey());

    final Unit existing = unit(10L, "KG", "Kilogram", "kg", false);
    existing.setCompany(company);
    when(unitService.findById(10L)).thenReturn(Optional.of(existing));
    when(productService.existsUsingUnit(10L)).thenReturn(true);
    final ApiException inUse = assertThrows(ApiException.class, () -> facade.deleteUnit(10L));
    assertEquals(ProductMessageKey.PRODUCT_UNIT_IN_USE, inUse.getMessageKey());
    verify(unitService, never()).delete(existing);
  }

  @Test
  void deletesUnusedCompanyUnit() {
    final Unit existing = unit(10L, "KG", "Kilogram", "kg", false);
    existing.setCompany(company);
    when(unitService.findById(10L)).thenReturn(Optional.of(existing));

    facade.deleteUnit(10L);

    verify(unitService).delete(existing);
  }

  @Test
  void createsCategoryWithCompanyAndDefaultSortOrder() {
    when(categoryService.save(any(ProductCategory.class))).thenAnswer(call -> call.getArgument(0));

    final var response =
        facade.createCategory(new CategoryRequest(null, " TOOLS ", " Tools ", "Hand tools", null));

    assertEquals("TOOLS", response.code());
    assertEquals("Tools", response.name());
    assertEquals(0, response.sortOrder());
    verify(categoryService)
        .save(
            org.mockito.ArgumentMatchers.argThat(
                category -> category.getCompany() == company && category.getParent() == null));
  }

  @Test
  void rejectsDuplicateCategoryAndSelfOrDescendantParenting() {
    lenient().when(categoryService.existsByCompanyIdAndCode(1L, "TOOLS")).thenReturn(true);
    final ApiException duplicate =
        assertThrows(
            ApiException.class,
            () -> facade.createCategory(new CategoryRequest(null, " TOOLS ", "Tools", null, 0)));
    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, duplicate.getMessageKey());

    final ProductCategory target = ProductCategory.builder().id(4L).code("A").name("A").build();
    when(categoryService.findByIdAndCompanyId(4L, 1L)).thenReturn(Optional.of(target));
    final ApiException selfParent =
        assertThrows(
            ApiException.class,
            () -> facade.updateCategory(4L, new CategoryRequest(4L, "A", "A", null, 0)));
    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, selfParent.getMessageKey());

    final ProductCategory descendant =
        ProductCategory.builder().id(5L).parent(target).code("B").name("B").build();
    when(categoryService.findByIdAndCompanyId(5L, 1L)).thenReturn(Optional.of(descendant));
    assertThrows(
        ApiException.class,
        () -> facade.updateCategory(4L, new CategoryRequest(5L, "A", "A", null, 0)));
  }

  @Test
  void updatesCategoryAndDeletesIt() {
    final ProductCategory parent =
        ProductCategory.builder().id(5L).code("ROOT").name("Root").build();
    final ProductCategory category =
        ProductCategory.builder().id(4L).code("OLD").name("Old").sortOrder(8).build();
    when(categoryService.findByIdAndCompanyId(4L, 1L)).thenReturn(Optional.of(category));
    when(categoryService.findByIdAndCompanyId(5L, 1L)).thenReturn(Optional.of(parent));
    when(categoryService.save(category)).thenReturn(category);

    final var response =
        facade.updateCategory(4L, new CategoryRequest(5L, " NEW ", " New ", "Updated", 3));

    assertEquals("NEW", response.code());
    assertEquals("New", response.name());
    assertEquals(5L, response.parentId());
    assertEquals(3, response.sortOrder());
    assertEquals("Updated", response.description());

    facade.deleteCategory(4L);
    verify(categoryService).delete(category);
  }

  @Test
  void allowsUpdatingCategoryWithItsCurrentCodeEvenWhenThatCodeExists() {
    final ProductCategory category =
        ProductCategory.builder().id(4L).code("TOOLS").name("Tools").build();
    when(categoryService.findByIdAndCompanyId(4L, 1L)).thenReturn(Optional.of(category));
    lenient().when(categoryService.existsByCompanyIdAndCode(1L, "TOOLS")).thenReturn(true);
    when(categoryService.save(category)).thenReturn(category);

    final var updated =
        facade.updateCategory(
            4L, new CategoryRequest(null, "TOOLS", "Workshop tools", "Revised", 4));

    assertEquals("TOOLS", updated.code());
    assertEquals("Revised", updated.description());
    assertEquals(4, updated.sortOrder());
  }

  @Test
  void createsAndUpdatesDefinitionsAndBlocksChangingUsedValueType() {
    when(definitionService.save(any(ProductAttributeDefinition.class)))
        .thenAnswer(call -> call.getArgument(0));
    final var created =
        facade.createDefinition(
            new AttributeDefinitionRequest(
                " COLOR ", " Color ", ProductAttributeValueType.TEXT, true));
    assertEquals("COLOR", created.code());
    assertTrue(created.required());

    final ProductAttributeDefinition existing =
        definition(30L, "COLOR", ProductAttributeValueType.TEXT, true);
    when(definitionService.findByIdAndCompanyId(30L, 1L)).thenReturn(Optional.of(existing));
    final var updated =
        facade.updateDefinition(
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
                facade.updateDefinition(
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
        facade.updateDefinition(
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
            facade.createDefinition(
                new AttributeDefinitionRequest(
                    " COLOR ", "Color", ProductAttributeValueType.TEXT, false)));

    final ProductAttributeDefinition definition =
        definition(30L, "COLOR", ProductAttributeValueType.TEXT, false);
    when(definitionService.findByIdAndCompanyId(30L, 1L)).thenReturn(Optional.of(definition));
    facade.deleteDefinition(30L);
    verify(definitionService).delete(definition);
  }

  @Test
  void createsUpdatesAndDeletesFixedOptions() {
    final ProductAttributeDefinition definition =
        definition(30L, "COLOR", ProductAttributeValueType.FIXED, false);
    when(definitionService.findByIdAndCompanyId(30L, 1L)).thenReturn(Optional.of(definition));
    when(optionService.save(any(ProductAttributeOption.class)))
        .thenAnswer(call -> call.getArgument(0));

    final var created = facade.createOption(30L, new AttributeOptionRequest(" Blue ", null));
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
    final var updated = facade.updateOption(30L, 40L, new AttributeOptionRequest(" Navy ", 2));
    assertEquals("Navy", updated.value());
    assertEquals(2, updated.sortOrder());

    facade.deleteOption(30L, 40L);
    verify(optionService).delete(option);
  }

  @Test
  void rejectsNonFixedDefinitionsDuplicateOptionsAndDeletingUsedOptions() {
    final ProductAttributeDefinition textDefinition =
        definition(30L, "NOT_FIXED", ProductAttributeValueType.TEXT, false);
    when(definitionService.findByIdAndCompanyId(30L, 1L)).thenReturn(Optional.of(textDefinition));
    assertThrows(
        ApiException.class, () -> facade.createOption(30L, new AttributeOptionRequest("Blue", 0)));

    final ProductAttributeDefinition fixedDefinition =
        definition(31L, "COLOR", ProductAttributeValueType.FIXED, false);
    when(definitionService.findByIdAndCompanyId(31L, 1L)).thenReturn(Optional.of(fixedDefinition));
    when(optionService.existsByDefinitionIdAndValue(31L, "Blue")).thenReturn(true);
    assertThrows(
        ApiException.class, () -> facade.createOption(31L, new AttributeOptionRequest("Blue", 0)));

    final ProductAttributeOption option =
        ProductAttributeOption.builder().id(41L).definition(fixedDefinition).value("Blue").build();
    when(optionService.findByIdAndDefinitionId(41L, 31L)).thenReturn(Optional.of(option));
    when(valueService.existsByOptionId(41L)).thenReturn(true);
    assertThrows(ApiException.class, () -> facade.deleteOption(31L, 41L));
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

  private Unit unit(
      final Long id,
      final String code,
      final String name,
      final String symbol,
      final boolean system) {
    return Unit.builder().id(id).code(code).name(name).symbol(symbol).system(system).build();
  }
}
