package dev.roland.inventory_management_backend.features.product_attribute_value.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;

/** Provides database queries for product attribute value records. */
public interface ProductAttributeValueRepository
    extends JpaRepository<ProductAttributeValue, Long> {
  List<ProductAttributeValue> findAllByProductId(Long productId);

  List<ProductAttributeValue> findAllByProductIdIn(List<Long> productIds);

  boolean existsByProductId(Long productId);

  boolean existsByOptionId(Long optionId);

  boolean existsByDefinitionId(Long definitionId);
}
