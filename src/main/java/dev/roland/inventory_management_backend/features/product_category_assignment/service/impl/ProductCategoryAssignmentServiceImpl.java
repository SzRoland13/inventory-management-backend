package dev.roland.inventory_management_backend.features.product_category_assignment.service.impl;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;
import dev.roland.inventory_management_backend.features.product_category.service.ProductCategoryService;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignment;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignmentId;
import dev.roland.inventory_management_backend.features.product_category_assignment.repository.ProductCategoryAssignmentRepository;
import dev.roland.inventory_management_backend.features.product_category_assignment.service.ProductCategoryAssignmentService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCategoryAssignmentServiceImpl implements ProductCategoryAssignmentService {
  private final ProductCategoryAssignmentRepository productCategoryAssignmentRepository;
  private final ProductService productService;
  private final ProductCategoryService productCategoryService;

  /** {@inheritDoc} */
  @Transactional
  @Override
  public ProductCategoryAssignment assign(Long productId, Long categoryId) {
    ProductCategoryAssignmentId id = new ProductCategoryAssignmentId(productId, categoryId);
    return productCategoryAssignmentRepository
        .findById(id)
        .orElseGet(() -> productCategoryAssignmentRepository.save(buildAssignment(id)));
  }

  /** {@inheritDoc} */
  @Transactional
  @Override
  public void remove(ProductCategoryAssignmentId id) {
    productCategoryAssignmentRepository.deleteById(id);
  }

  private ProductCategoryAssignment buildAssignment(ProductCategoryAssignmentId id) {
    Product product = productService.findByIdOrThrow(id.getProductId());
    ProductCategory category = productCategoryService.findByIdOrThrow(id.getCategoryId());

    return ProductCategoryAssignment.builder().id(id).product(product).category(category).build();
  }
}
