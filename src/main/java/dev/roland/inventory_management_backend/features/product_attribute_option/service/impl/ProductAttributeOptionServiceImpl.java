package dev.roland.inventory_management_backend.features.product_attribute_option.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;
import dev.roland.inventory_management_backend.features.product_attribute_option.repository.ProductAttributeOptionRepository;
import dev.roland.inventory_management_backend.features.product_attribute_option.service.ProductAttributeOptionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductAttributeOptionServiceImpl implements ProductAttributeOptionService {
  private final ProductAttributeOptionRepository productAttributeOptionRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<ProductAttributeOption, Long> getRepository() {
    return productAttributeOptionRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.PRODUCT_ATTRIBUTE_OPTION;
  }
}
