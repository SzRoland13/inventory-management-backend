package dev.roland.inventory_management_backend.features.product_category.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import dev.roland.inventory_management_backend.features.product.dto.CategoryRequest;
import dev.roland.inventory_management_backend.features.product.mapper.ProductSettingsMapperImpl;
import dev.roland.inventory_management_backend.features.product.message.ProductMessageKey;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;
import dev.roland.inventory_management_backend.features.product_category.dto.CategoryReorderRequest;
import dev.roland.inventory_management_backend.features.product_category.service.ProductCategoryService;

@ExtendWith(MockitoExtension.class)
class ProductCategoryFacadeImplTest {

  @Mock private ProductCategoryService categoryService;
  @Mock private CompanyService companyService;

  private ProductCategoryFacadeImpl categoryFacade;
  private Company company;

  @BeforeEach
  void setUp() {
    categoryFacade =
        new ProductCategoryFacadeImpl(
            categoryService, companyService, new ProductSettingsMapperImpl());
    company = Company.builder().id(1L).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
  }

  @Test
  void listsCompanyCategories() {
    when(categoryService.findAllByCompanyId(1L))
        .thenReturn(List.of(ProductCategory.builder().id(20L).code("TOOLS").name("Tools").build()));
    assertEquals(20L, categoryFacade.list().getFirst().id());
  }

  @Test
  void createsCategoryWithCompanyAndDefaultSortOrder() {
    when(categoryService.save(any(ProductCategory.class))).thenAnswer(call -> call.getArgument(0));

    final var response =
        categoryFacade.create(new CategoryRequest(null, " TOOLS ", " Tools ", "Hand tools", null));

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
            () -> categoryFacade.create(new CategoryRequest(null, " TOOLS ", "Tools", null, 0)));
    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, duplicate.getMessageKey());

    final ProductCategory target = ProductCategory.builder().id(4L).code("A").name("A").build();
    when(categoryService.findByIdAndCompanyId(4L, 1L)).thenReturn(Optional.of(target));
    final ApiException selfParent =
        assertThrows(
            ApiException.class,
            () -> categoryFacade.update(4L, new CategoryRequest(4L, "A", "A", null, 0)));
    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, selfParent.getMessageKey());

    final ProductCategory descendant =
        ProductCategory.builder().id(5L).parent(target).code("B").name("B").build();
    when(categoryService.findByIdAndCompanyId(5L, 1L)).thenReturn(Optional.of(descendant));
    assertThrows(
        ApiException.class,
        () -> categoryFacade.update(4L, new CategoryRequest(5L, "A", "A", null, 0)));
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
        categoryFacade.update(4L, new CategoryRequest(5L, " NEW ", " New ", "Updated", 3));

    assertEquals("NEW", response.code());
    assertEquals("New", response.name());
    assertEquals(5L, response.parentId());
    assertEquals(3, response.sortOrder());
    assertEquals("Updated", response.description());

    categoryFacade.delete(4L);
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
        categoryFacade.update(
            4L, new CategoryRequest(null, "TOOLS", "Workshop tools", "Revised", 4));

    assertEquals("TOOLS", updated.code());
    assertEquals("Revised", updated.description());
    assertEquals(4, updated.sortOrder());
  }

  @Test
  void reordersRootCategoriesAndReturnsThemInRequestedOrder() {
    final ProductCategory first =
        ProductCategory.builder().id(10L).name("First").sortOrder(0).build();
    final ProductCategory second =
        ProductCategory.builder().id(20L).name("Second").sortOrder(1).build();
    when(categoryService.findAllByCompanyId(1L)).thenReturn(List.of(first, second));
    when(categoryService.saveAll(anyList())).thenAnswer(call -> call.getArgument(0));

    final var reordered = categoryFacade.reorder(new CategoryReorderRequest(List.of(20L, 10L)));

    assertEquals(List.of(20L, 10L), reordered.stream().map(response -> response.id()).toList());
    assertEquals(0, second.getSortOrder());
    assertEquals(1, first.getSortOrder());
    verify(categoryService)
        .saveAll(
            org.mockito.ArgumentMatchers.argThat(
                categories -> categories.equals(List.of(second, first))));
  }

  @Test
  void reordersChildrenWithinTheirParentGroup() {
    final ProductCategory parent = ProductCategory.builder().id(1L).name("Parent").build();
    final ProductCategory first =
        ProductCategory.builder().id(11L).name("First").parent(parent).sortOrder(0).build();
    final ProductCategory second =
        ProductCategory.builder().id(12L).name("Second").parent(parent).sortOrder(1).build();
    when(categoryService.findAllByCompanyId(1L)).thenReturn(List.of(parent, first, second));
    when(categoryService.saveAll(anyList())).thenAnswer(call -> call.getArgument(0));

    final var reordered = categoryFacade.reorder(new CategoryReorderRequest(List.of(12L, 11L)));

    assertEquals(List.of(12L, 11L), reordered.stream().map(response -> response.id()).toList());
    assertEquals(1L, reordered.getFirst().parentId());
    assertEquals(0, second.getSortOrder());
    assertEquals(1, first.getSortOrder());
  }

  @Test
  void rejectsCrossCompanyAndMixedParentReordersWithoutSaving() {
    final ProductCategory root = ProductCategory.builder().id(1L).name("Root").sortOrder(4).build();
    final ProductCategory otherRoot =
        ProductCategory.builder().id(2L).name("Other root").sortOrder(5).build();
    final ProductCategory child =
        ProductCategory.builder().id(3L).name("Child").parent(root).sortOrder(6).build();
    when(categoryService.findAllByCompanyId(1L)).thenReturn(List.of(root, otherRoot, child));

    assertThrows(
        ApiException.class,
        () -> categoryFacade.reorder(new CategoryReorderRequest(List.of(1L, 999L))));
    assertThrows(
        ApiException.class,
        () -> categoryFacade.reorder(new CategoryReorderRequest(List.of(1L, 3L))));

    assertEquals(4, root.getSortOrder());
    assertEquals(6, child.getSortOrder());
    verify(categoryService, never()).saveAll(anyList());
  }

  @Test
  void rejectsIncompleteOrDuplicateSiblingListsWithoutSaving() {
    final ProductCategory first =
        ProductCategory.builder().id(1L).name("First").sortOrder(4).build();
    final ProductCategory second =
        ProductCategory.builder().id(2L).name("Second").sortOrder(5).build();
    when(categoryService.findAllByCompanyId(1L)).thenReturn(List.of(first, second));

    assertThrows(
        ApiException.class, () -> categoryFacade.reorder(new CategoryReorderRequest(List.of(1L))));
    assertThrows(
        ApiException.class,
        () -> categoryFacade.reorder(new CategoryReorderRequest(List.of(1L, 1L))));
    assertThrows(
        ApiException.class, () -> categoryFacade.reorder(new CategoryReorderRequest(List.of())));

    assertEquals(4, first.getSortOrder());
    assertEquals(5, second.getSortOrder());
    verify(categoryService, never()).saveAll(anyList());
  }
}
