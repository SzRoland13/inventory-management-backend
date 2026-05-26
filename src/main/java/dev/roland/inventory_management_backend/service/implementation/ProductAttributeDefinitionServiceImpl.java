package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.ProductAttributeDefinition;
import dev.roland.inventory_management_backend.repository.ProductAttributeDefinitionRepository;
import dev.roland.inventory_management_backend.service.ProductAttributeDefinitionService;
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
