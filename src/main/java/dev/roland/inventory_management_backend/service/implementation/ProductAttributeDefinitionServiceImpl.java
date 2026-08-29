package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;
import dev.roland.inventory_management_backend.features.product_attribute_definition.repository.ProductAttributeDefinitionRepository;
import dev.roland.inventory_management_backend.features.product_attribute_definition.service.ProductAttributeDefinitionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductAttributeDefinitionServiceImpl implements ProductAttributeDefinitionService {
  private final ProductAttributeDefinitionRepository productAttributeDefinitionRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<ProductAttributeDefinition, Long> getRepository() {
    return productAttributeDefinitionRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.PRODUCT_ATTRIBUTE_DEFINITION;
  }
}
