package dev.roland.inventory_management_backend.features.product_category.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;
import dev.roland.inventory_management_backend.features.product_category.repository.ProductCategoryRepository;
import dev.roland.inventory_management_backend.features.product_category.service.ProductCategoryService;
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
