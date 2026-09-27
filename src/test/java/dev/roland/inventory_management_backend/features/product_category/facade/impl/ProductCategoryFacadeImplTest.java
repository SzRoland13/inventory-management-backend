package dev.roland.inventory_management_backend.features.product_category.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
}
