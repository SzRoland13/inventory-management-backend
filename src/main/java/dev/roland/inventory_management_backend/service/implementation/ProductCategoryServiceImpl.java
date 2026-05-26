package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.ProductCategory;
import dev.roland.inventory_management_backend.repository.ProductCategoryRepository;
import dev.roland.inventory_management_backend.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {
  private final ProductCategoryRepository productCategoryRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<ProductCategory, Long> getRepository() {
    return productCategoryRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.PRODUCT_CATEGORY;
  }
}
