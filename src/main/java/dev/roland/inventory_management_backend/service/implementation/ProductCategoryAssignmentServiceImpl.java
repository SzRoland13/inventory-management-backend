package dev.roland.inventory_management_backend.service.implementation;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.model.Product;
import dev.roland.inventory_management_backend.model.ProductCategory;
import dev.roland.inventory_management_backend.model.ProductCategoryAssignment;
import dev.roland.inventory_management_backend.model.ProductCategoryAssignmentId;
import dev.roland.inventory_management_backend.repository.ProductCategoryAssignmentRepository;
import dev.roland.inventory_management_backend.service.ProductCategoryAssignmentService;
import dev.roland.inventory_management_backend.service.ProductCategoryService;
import dev.roland.inventory_management_backend.service.ProductService;
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
