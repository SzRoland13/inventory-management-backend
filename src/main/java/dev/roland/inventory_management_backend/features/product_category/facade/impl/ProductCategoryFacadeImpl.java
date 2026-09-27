package dev.roland.inventory_management_backend.features.product_category.facade.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.product.dto.CategoryRequest;
import dev.roland.inventory_management_backend.features.product.dto.CategoryResponse;
import dev.roland.inventory_management_backend.features.product.mapper.ProductSettingsMapper;
import dev.roland.inventory_management_backend.features.product.message.ProductMessageKey;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;
import dev.roland.inventory_management_backend.features.product_category.dto.CategoryReorderRequest;
import dev.roland.inventory_management_backend.features.product_category.facade.ProductCategoryFacade;
import dev.roland.inventory_management_backend.features.product_category.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;

/** Implements product category operations. */
@Service
@RequiredArgsConstructor
public class ProductCategoryFacadeImpl implements ProductCategoryFacade {
  private final ProductCategoryService categoryService;
  private final CompanyService companyService;
  private final ProductSettingsMapper mapper;

  @Override
  @Transactional
  public List<CategoryResponse> list() {
    return categoryService.findAllByCompanyId(companyId()).stream()
        .sorted(categoryDisplayOrder())
        .map(mapper::toCategoryResponse)
        .toList();
  }

  @Override
  @Transactional
  public List<CategoryResponse> reorder(final CategoryReorderRequest request) {
    final List<Long> categoryIds = request.categoryIds();
    if (categoryIds == null || categoryIds.isEmpty()) {
      throw invalid();
    }

    final Set<Long> requestedIds = new HashSet<>(categoryIds);
    if (requestedIds.size() != categoryIds.size()) {
      throw invalid();
    }

    final List<ProductCategory> companyCategories = categoryService.findAllByCompanyId(companyId());
    final Map<Long, ProductCategory> categoriesById =
        companyCategories.stream()
            .collect(Collectors.toMap(ProductCategory::getId, Function.identity()));
    final List<ProductCategory> orderedCategories = new ArrayList<>(categoryIds.size());
    for (Long categoryId : categoryIds) {
      final ProductCategory category = categoriesById.get(categoryId);
      if (category == null) {
        throw invalid();
      }
      orderedCategories.add(category);
    }

    final Long parentId = parentId(orderedCategories.getFirst());
    final List<ProductCategory> siblings =
        companyCategories.stream()
            .filter(category -> Objects.equals(parentId(category), parentId))
            .toList();
    if (siblings.size() != categoryIds.size()
        || !requestedIds.containsAll(siblings.stream().map(ProductCategory::getId).toList())) {
      throw invalid();
    }

    for (int index = 0; index < orderedCategories.size(); index++) {
      orderedCategories.get(index).setSortOrder(index);
    }
    return categoryService.saveAll(orderedCategories).stream()
        .map(mapper::toCategoryResponse)
        .toList();
  }

  @Override
  @Transactional
  public CategoryResponse create(final CategoryRequest request) {
    final var company = companyService.getCompanyOrCreateNew();
    final String code = request.code().trim();
    if (categoryService.existsByCompanyIdAndCode(company.getId(), code)) {
      throw invalid();
    }
    final ProductCategory category =
        ProductCategory.builder()
            .company(company)
            .parent(findParent(request.parentId()))
            .code(code)
            .name(request.name().trim())
            .description(request.description())
            .sortOrder(Objects.requireNonNullElse(request.sortOrder(), 0))
            .build();
    return mapper.toCategoryResponse(categoryService.save(category));
  }

  @Override
  @Transactional
  public CategoryResponse update(final Long id, final CategoryRequest request) {
    final ProductCategory category = findCategory(id);
    final String code = request.code().trim();
    if (!category.getCode().equals(code)
        && categoryService.existsByCompanyIdAndCode(companyId(), code)) {
      throw invalid();
    }
    final ProductCategory parent = findParent(request.parentId());
    if (parent != null && parent.getId().equals(id)) {
      throw invalid();
    }
    ProductCategory ancestor = parent;
    while (ancestor != null) {
      if (ancestor.getId().equals(id)) {
        throw invalid();
      }
      ancestor = ancestor.getParent();
    }
    category.setParent(parent);
    category.setCode(code);
    category.setName(request.name().trim());
    category.setDescription(request.description());
    category.setSortOrder(Objects.requireNonNullElse(request.sortOrder(), 0));
    return mapper.toCategoryResponse(categoryService.save(category));
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    categoryService.delete(findCategory(id));
  }

  private ProductCategory findCategory(final Long id) {
    return categoryService.findByIdAndCompanyId(id, companyId()).orElseThrow(this::invalid);
  }

  private ProductCategory findParent(final Long parentId) {
    return parentId == null ? null : findCategory(parentId);
  }

  private Long companyId() {
    return companyService.getCompanyOrCreateNew().getId();
  }

  private static Long parentId(final ProductCategory category) {
    return category.getParent() == null ? null : category.getParent().getId();
  }

  private static Comparator<ProductCategory> categoryDisplayOrder() {
    return Comparator.comparing(
            ProductCategoryFacadeImpl::parentId, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(
            ProductCategory::getSortOrder, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(ProductCategory::getId, Comparator.nullsFirst(Comparator.naturalOrder()));
  }

  private ApiException invalid() {
    return new ApiException(ProductMessageKey.INVALID_PRODUCT_DATA);
  }
}
