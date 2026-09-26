package dev.roland.inventory_management_backend.features.product.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.dto.ProductListRequest;
import dev.roland.inventory_management_backend.features.product.repository.ProductRepository;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignment;
import lombok.RequiredArgsConstructor;

/** Implements the product catalog service operations. */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
  private final ProductRepository productRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<Product, Long> getRepository() {
    return productRepository;
  }

  /**
   * {@inheritDoc}
   *
   * @return get not found message key result
   */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.PRODUCT;
  }

  @Override
  public Page<Product> search(
      final Long companyId, final boolean archived, final ProductListRequest request) {
    final Specification<Product> specification =
        (root, query, criteriaBuilder) -> {
          final List<Predicate> predicates = new ArrayList<>();
          predicates.add(criteriaBuilder.equal(root.get("company").get("id"), companyId));
          predicates.add(
              archived
                  ? criteriaBuilder.isNotNull(root.get("deletedAt"))
                  : criteriaBuilder.isNull(root.get("deletedAt")));
          if (request.status() != null) {
            predicates.add(criteriaBuilder.equal(root.get("status"), request.status()));
          }
          if (request.brand() != null && !request.brand().isBlank()) {
            predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("brand")), pattern(request.brand())));
          }
          if (request.unitId() != null) {
            predicates.add(
                criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("unit").get("id"), request.unitId()),
                    criteriaBuilder.equal(root.get("secondaryUnit").get("id"), request.unitId())));
          }
          if (request.categoryId() != null) {
            final Subquery<Integer> assignmentQuery = query.subquery(Integer.class);
            final var assignment = assignmentQuery.from(ProductCategoryAssignment.class);
            assignmentQuery.select(criteriaBuilder.literal(1));
            assignmentQuery.where(
                criteriaBuilder.equal(assignment.get("product").get("id"), root.get("id")),
                criteriaBuilder.equal(assignment.get("category").get("id"), request.categoryId()));
            predicates.add(criteriaBuilder.exists(assignmentQuery));
          }
          if (request.search() != null && !request.search().isBlank()) {
            final String pattern = pattern(request.search());
            predicates.add(
                criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("ean")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)));
          }
          return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    final Sort sort =
        Sort.by(
                Sort.Direction.valueOf(request.sortDirection().name()),
                request.sortBy().getProperty())
            .and(Sort.by(Sort.Direction.ASC, "id"));
    return productRepository.findAll(
        specification, PageRequest.of(request.page(), request.size(), sort));
  }

  @Override
  public java.util.Optional<Product> findByIdAndCompanyId(final Long id, final Long companyId) {
    return productRepository.findByIdAndCompanyId(id, companyId);
  }

  @Override
  public boolean existsByCompanyIdAndSku(final Long companyId, final String sku) {
    return productRepository.existsByCompanyIdAndSku(companyId, sku);
  }

  @Override
  public boolean existsByCompanyIdAndSkuAndIdNot(
      final Long companyId, final String sku, final Long id) {
    return productRepository.existsByCompanyIdAndSkuAndIdNot(companyId, sku, id);
  }

  @Override
  public boolean existsUsingUnit(final Long unitId) {
    return productRepository.existsUsingUnit(unitId);
  }

  private String pattern(final String value) {
    return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
  }
}
