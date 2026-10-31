package dev.roland.inventory_management_backend.features.product_attribute_value.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;
import dev.roland.inventory_management_backend.features.product_attribute_value.repository.ProductAttributeValueRepository;
import dev.roland.inventory_management_backend.features.product_attribute_value.service.ProductAttributeValueService;
import lombok.RequiredArgsConstructor;

/** Implements the product attribute value service operations. */
@Service
@RequiredArgsConstructor
public class ProductAttributeValueServiceImpl implements ProductAttributeValueService {
  private final ProductAttributeValueRepository productAttributeValueRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<ProductAttributeValue, Long> getRepository() {
    return productAttributeValueRepository;
  }

  /**
   * {@inheritDoc}
   *
   * @return get not found message key result
   */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.PRODUCT_ATTRIBUTE_VALUE;
  }

  @Override
  public List<ProductAttributeValue> findAllByProductId(final Long id) {
    return productAttributeValueRepository.findAllByProductId(id);
  }

  @Override
  public List<ProductAttributeValue> findAllByProductIdIn(final List<Long> productIds) {
    return productAttributeValueRepository.findAllByProductIdIn(productIds);
  }

  @Override
  public void deleteAllByProductId(final Long id) {
    productAttributeValueRepository.deleteAll(
        productAttributeValueRepository.findAllByProductId(id));
  }

  @Override
  public boolean existsByOptionId(final Long id) {
    return productAttributeValueRepository.existsByOptionId(id);
  }

  @Override
  public boolean existsByDefinitionId(final Long id) {
    return productAttributeValueRepository.existsByDefinitionId(id);
  }
}
