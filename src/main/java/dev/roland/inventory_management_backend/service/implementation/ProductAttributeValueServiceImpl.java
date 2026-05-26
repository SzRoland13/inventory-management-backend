package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.ProductAttributeValue;
import dev.roland.inventory_management_backend.repository.ProductAttributeValueRepository;
import dev.roland.inventory_management_backend.service.ProductAttributeValueService;
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
