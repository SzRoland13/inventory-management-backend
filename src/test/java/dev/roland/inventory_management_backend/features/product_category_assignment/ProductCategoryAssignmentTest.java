package dev.roland.inventory_management_backend.features.product_category_assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;

class ProductCategoryAssignmentTest {

  @Test
  void createsIdentifierOnlyWhenBothRelationshipsExist() {
    final Product product = Product.builder().id(7L).build();
    final ProductCategory category = ProductCategory.builder().id(11L).build();
    final ProductCategoryAssignment assignment =
        ProductCategoryAssignment.builder().product(product).category(category).build();

    assignment.ensureId();

    assertEquals(new ProductCategoryAssignmentId(7L, 11L), assignment.getId());

    assignment.setId(new ProductCategoryAssignmentId(1L, 2L));
    assignment.ensureId();
    assertEquals(new ProductCategoryAssignmentId(1L, 2L), assignment.getId());

    final ProductCategoryAssignment missingCategory =
        ProductCategoryAssignment.builder().product(product).build();
    missingCategory.ensureId();
    assertNull(missingCategory.getId());

    final ProductCategoryAssignment missingProduct =
        ProductCategoryAssignment.builder().category(category).build();
    missingProduct.ensureId();
    assertNull(missingProduct.getId());
  }
}
