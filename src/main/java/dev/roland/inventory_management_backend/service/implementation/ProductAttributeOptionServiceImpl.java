package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.ProductAttributeOption;
import dev.roland.inventory_management_backend.repository.ProductAttributeOptionRepository;
import dev.roland.inventory_management_backend.service.ProductAttributeOptionService;
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
