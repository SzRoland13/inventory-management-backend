package dev.roland.inventory_management_backend.features.product_attribute_option.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;
import dev.roland.inventory_management_backend.features.product_attribute_option.repository.ProductAttributeOptionRepository;
import dev.roland.inventory_management_backend.features.product_attribute_option.service.ProductAttributeOptionService;
import lombok.RequiredArgsConstructor;

/** Implements the product attribute option service operations. */
@Service
@RequiredArgsConstructor
public class ProductAttributeOptionServiceImpl implements ProductAttributeOptionService {
  private final ProductAttributeOptionRepository productAttributeOptionRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<ProductAttributeOption, Long> getRepository() {
    return productAttributeOptionRepository;
  }

  /**
   * {@inheritDoc}
   *
   * @return get not found message key result
   */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.PRODUCT_ATTRIBUTE_OPTION;
  }

  @Override
  public java.util.List<ProductAttributeOption> findAllByDefinitionId(Long id) {
    return productAttributeOptionRepository.findAllByDefinitionId(id);
  }

  @Override
  public java.util.Optional<ProductAttributeOption> findByIdAndDefinitionId(
      Long id, Long definitionId) {
    return productAttributeOptionRepository
        .findById(id)
        .filter(option -> option.getDefinition().getId().equals(definitionId));
  }

  @Override
  public boolean existsByDefinitionId(Long id) {
    return productAttributeOptionRepository.existsByDefinitionId(id);
  }

  @Override
  public boolean existsByDefinitionIdAndValue(Long id, String value) {
    return productAttributeOptionRepository.existsByDefinitionIdAndValue(id, value);
  }

  @Override
  public boolean existsByDefinitionIdAndValueAndIdNot(Long id, String value, Long optionId) {
    return productAttributeOptionRepository.existsByDefinitionIdAndValueAndIdNot(
        id, value, optionId);
  }
}
