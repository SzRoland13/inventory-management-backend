package dev.roland.inventory_management_backend.features.product_attribute_value.service;

import java.util.List;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;

/** Defines operations supported by the product attribute value feature. */
public interface ProductAttributeValueService extends BaseService<ProductAttributeValue, Long> {
  List<ProductAttributeValue> findAllByProductId(Long productId);

  List<ProductAttributeValue> findAllByProductIdIn(List<Long> productIds);

  void deleteAllByProductId(Long productId);

  boolean existsByOptionId(Long optionId);

  boolean existsByDefinitionId(Long definitionId);
}
