package dev.roland.inventory_management_backend.features.product_attribute_definition.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;
import dev.roland.inventory_management_backend.features.product_attribute_definition.repository.ProductAttributeDefinitionRepository;
import dev.roland.inventory_management_backend.features.product_attribute_definition.service.ProductAttributeDefinitionService;
import lombok.RequiredArgsConstructor;

/** Implements the product attribute definition service operations. */
@Service
@RequiredArgsConstructor
public class ProductAttributeDefinitionServiceImpl implements ProductAttributeDefinitionService {
  private final ProductAttributeDefinitionRepository productAttributeDefinitionRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<ProductAttributeDefinition, Long> getRepository() {
    return productAttributeDefinitionRepository;
  }

  /**
   * {@inheritDoc}
   *
   * @return get not found message key result
   */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.PRODUCT_ATTRIBUTE_DEFINITION;
  }

  @Override
  public java.util.List<ProductAttributeDefinition> findAllByCompanyId(final Long id) {
    return productAttributeDefinitionRepository.findAllByCompanyId(id);
  }

  @Override
  public java.util.Optional<ProductAttributeDefinition> findByIdAndCompanyId(
      final Long id, final Long companyId) {
    return productAttributeDefinitionRepository.findByIdAndCompanyId(id, companyId);
  }

  @Override
  public boolean existsByCompanyIdAndCode(final Long companyId, final String code) {
    return productAttributeDefinitionRepository.existsByCompanyIdAndCode(companyId, code);
  }
}
