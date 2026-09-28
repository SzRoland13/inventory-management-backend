package dev.roland.inventory_management_backend.features.product_attribute_value.service;

import java.util.List;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;

/** Defines operations supported by the product attribute value feature. */
public interface ProductAttributeValueService extends BaseService<ProductAttributeValue, Long> {
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
   * Deletes the matching record or records.
   *
   * @param productId the product identifier
   */
  void deleteAllByProductId(Long productId);

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
