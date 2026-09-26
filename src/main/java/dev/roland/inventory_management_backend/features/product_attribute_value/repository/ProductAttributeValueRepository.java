package dev.roland.inventory_management_backend.features.product_attribute_value.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;

/** Provides database queries for product attribute value records. */
public interface ProductAttributeValueRepository
    extends JpaRepository<ProductAttributeValue, Long> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param productId the product identifier
   * @return the matching resources
   */
  List<ProductAttributeValue> findAllByProductId(Long productId);

  /**
   * Finds matching records using the supplied criteria.
   *
   * @param productIds the product identifiers
   * @return the matching resources
   */
  List<ProductAttributeValue> findAllByProductIdIn(List<Long> productIds);

  /**
   * Checks whether a matching record exists.
   *
   * @param productId the product identifier
   * @return true if a matching record exists
   */
  boolean existsByProductId(Long productId);

  /**
   * Checks whether a matching record exists.
   *
   * @param optionId the attribute option identifier
   * @return true if a matching record exists
   */
  boolean existsByOptionId(Long optionId);

  /**
   * Checks whether a matching record exists.
   *
   * @param definitionId the attribute definition identifier
   * @return true if a matching record exists
   */
  boolean existsByDefinitionId(Long definitionId);
}
