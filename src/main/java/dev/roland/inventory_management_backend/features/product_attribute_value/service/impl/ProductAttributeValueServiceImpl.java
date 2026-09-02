package dev.roland.inventory_management_backend.features.product_attribute_value.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;
import dev.roland.inventory_management_backend.features.product_attribute_value.repository.ProductAttributeValueRepository;
import dev.roland.inventory_management_backend.features.product_attribute_value.service.ProductAttributeValueService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductAttributeValueServiceImpl implements ProductAttributeValueService {
  private final ProductAttributeValueRepository productAttributeValueRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<ProductAttributeValue, Long> getRepository() {
    return productAttributeValueRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.PRODUCT_ATTRIBUTE_VALUE;
  }
}
