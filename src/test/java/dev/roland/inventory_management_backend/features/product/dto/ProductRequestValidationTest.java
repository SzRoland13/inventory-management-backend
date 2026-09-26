package dev.roland.inventory_management_backend.features.product.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.features.product.enumeration.ProductSortDirection;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductSortField;

class ProductRequestValidationTest {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void acceptsMainUnitOnlyAndValidSecondaryUnitConfigurations() {
    assertTrue(validator.validate(new ProductRequest.Units(1L, null, null)).isEmpty());
    assertTrue(
        validator.validate(new ProductRequest.Units(1L, 2L, new BigDecimal("12"))).isEmpty());
  }

  @Test
  void rejectsIncompleteEqualAndNonPositiveSecondaryUnitConfigurations() {
    assertFalse(validator.validate(new ProductRequest.Units(1L, 2L, null)).isEmpty());
    assertFalse(validator.validate(new ProductRequest.Units(1L, null, BigDecimal.ONE)).isEmpty());
    assertFalse(validator.validate(new ProductRequest.Units(1L, 1L, BigDecimal.ONE)).isEmpty());
    assertFalse(validator.validate(new ProductRequest.Units(1L, 2L, BigDecimal.ZERO)).isEmpty());
    assertFalse(validator.validate(new ProductRequest.Units(null, 2L, BigDecimal.ONE)).isEmpty());
    assertFalse(new ProductRequest.Units(1L, 2L, BigDecimal.ZERO).isConfigurationValid());
  }

  @Test
  void appliesPaginationAndSortDefaultsOnlyToOmittedValues() {
    ProductListRequest defaults =
        new ProductListRequest(null, null, null, null, null, null, null, null, null);
    assertTrue(defaults.page() == 0);
    assertTrue(defaults.size() == 25);
    assertTrue(defaults.sortBy() == ProductSortField.NAME);
    assertTrue(defaults.sortDirection() == ProductSortDirection.ASC);

    ProductListRequest provided =
        new ProductListRequest(
            3, 40, "x", "brand", null, null, null, ProductSortField.SKU, ProductSortDirection.DESC);
    assertTrue(provided.page() == 3);
    assertTrue(provided.size() == 40);
    assertTrue(provided.sortBy() == ProductSortField.SKU);
    assertTrue(provided.sortDirection() == ProductSortDirection.DESC);
  }

  @Test
  void productAttributeRequiresExactlyOneTypedValue() {
    assertTrue(
        validator
            .validate(new ProductAttributeValueRequest(1L, null, "blue", null, null, null))
            .isEmpty());
    assertFalse(
        validator
            .validate(new ProductAttributeValueRequest(1L, null, null, null, null, null))
            .isEmpty());
    assertFalse(
        validator
            .validate(new ProductAttributeValueRequest(1L, 2L, "blue", null, null, null))
            .isEmpty());
    assertFalse(
        validator
            .validate(new ProductAttributeValueRequest(1L, null, "", BigDecimal.ONE, null, null))
            .isEmpty());
    assertTrue(
        validator
            .validate(new ProductAttributeValueRequest(1L, 2L, null, null, null, null))
            .isEmpty());
    assertTrue(
        validator
            .validate(new ProductAttributeValueRequest(1L, null, null, BigDecimal.ONE, null, null))
            .isEmpty());
    assertTrue(
        validator
            .validate(new ProductAttributeValueRequest(1L, null, null, null, LocalDate.now(), null))
            .isEmpty());
    assertTrue(
        validator
            .validate(new ProductAttributeValueRequest(1L, null, null, null, null, false))
            .isEmpty());
  }

  @Test
  void defensivelyCopiesOptionalProductCollections() {
    var categories = new java.util.ArrayList<>(java.util.List.of(1L));
    var attributes = new java.util.ArrayList<ProductAttributeValueRequest>();
    ProductRequest request =
        new ProductRequest(
            "SKU",
            null,
            "Name",
            null,
            null,
            null,
            new ProductRequest.Units(1L, null, null),
            new ProductRequest.Pricing(null, BigDecimal.ONE, null, BigDecimal.ZERO),
            null,
            categories,
            attributes);

    categories.add(2L);
    assertTrue(request.categoryIds().equals(java.util.List.of(1L)));
    assertFalse(request.categoryIds() == categories);
    assertTrue(request.attributes().isEmpty());
    assertTrue(request.categoryIds() != request.categoryIds());
  }
}
