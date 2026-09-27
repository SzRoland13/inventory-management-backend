package dev.roland.inventory_management_backend.features.product.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import dev.roland.inventory_management_backend.features.brand.Brand;
import dev.roland.inventory_management_backend.features.brand.repository.BrandRepository;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.repository.CompanyRepository;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.dto.ProductListRequest;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductSortDirection;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductSortField;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.product.repository.ProductRepository;
import dev.roland.inventory_management_backend.features.product_category.ProductCategory;
import dev.roland.inventory_management_backend.features.product_category.repository.ProductCategoryRepository;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignment;
import dev.roland.inventory_management_backend.features.product_category_assignment.ProductCategoryAssignmentId;
import dev.roland.inventory_management_backend.features.product_category_assignment.repository.ProductCategoryAssignmentRepository;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.repository.UnitRepository;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductServiceImplTest {
  @Autowired private ProductRepository productRepository;
  @Autowired private BrandRepository brandRepository;
  @Autowired private CompanyRepository companyRepository;
  @Autowired private UnitRepository unitRepository;
  @Autowired private ProductCategoryRepository categoryRepository;
  @Autowired private ProductCategoryAssignmentRepository assignmentRepository;

  private ProductServiceImpl productService;
  private Company company;
  private Unit each;
  private Unit box;
  private Unit pair;
  private ProductCategory gloves;

  @BeforeEach
  void setUp() {
    productService = new ProductServiceImpl(productRepository);
    company = companyRepository.save(Company.builder().name("Acme").build());
    each = unitRepository.save(unit("EA"));
    box = unitRepository.save(unit("BOX"));
    pair = unitRepository.save(unit("PAIR"));
    gloves =
        categoryRepository.save(
            ProductCategory.builder().company(company).code("GLOVES").name("Gloves").build());
  }

  @Test
  void searchCombinesArchiveStatusBrandEitherUnitCategoryAndTextFilters() {
    final Product target =
        saveProduct(
            "GLOVE-BLUE",
            "Blue nitrile gloves",
            "Safety",
            "blue disposable glove",
            ProductStatus.ACTIVE,
            box,
            pair,
            LocalDateTime.now());
    assign(target, gloves);
    final Product searchMismatch =
        saveProduct(
            "GLOVE-OTHER",
            "Nitrile gloves",
            "Safety",
            "disposable glove",
            ProductStatus.ACTIVE,
            box,
            pair,
            LocalDateTime.now());
    assign(searchMismatch, gloves);
    final Product statusMismatch =
        saveProduct(
            "GLOVE-STATUS",
            "Blue nitrile gloves",
            "Safety",
            "blue disposable glove",
            ProductStatus.DISCONTINUED,
            box,
            pair,
            LocalDateTime.now());
    assign(statusMismatch, gloves);
    final Product brandMismatch =
        saveProduct(
            "GLOVE-BRAND",
            "Blue nitrile gloves",
            "Other",
            "blue disposable glove",
            ProductStatus.ACTIVE,
            box,
            pair,
            LocalDateTime.now());
    assign(brandMismatch, gloves);
    final Product unitMismatch =
        saveProduct(
            "GLOVE-UNIT",
            "Blue nitrile gloves",
            "Safety",
            "blue disposable glove",
            ProductStatus.ACTIVE,
            each,
            null,
            LocalDateTime.now());
    assign(unitMismatch, gloves);
    final Product categoryMismatch =
        saveProduct(
            "GLOVE-CATEGORY",
            "Blue nitrile gloves",
            "Safety",
            "blue disposable glove",
            ProductStatus.ACTIVE,
            box,
            pair,
            LocalDateTime.now());
    final ProductCategory otherCategory =
        categoryRepository.save(
            ProductCategory.builder().company(company).code("MASKS").name("Masks").build());
    assign(categoryMismatch, otherCategory);
    final Product activeMismatch =
        saveProduct(
            "GLOVE-ACTIVE",
            "Blue nitrile gloves",
            "Safety",
            "blue disposable glove",
            ProductStatus.ACTIVE,
            box,
            pair,
            null);
    assign(activeMismatch, gloves);

    final ProductListRequest request =
        new ProductListRequest(
            0,
            20,
            " BLUE ",
            "Safety",
            ProductStatus.ACTIVE,
            gloves.getId(),
            pair.getId(),
            ProductSortField.NAME,
            ProductSortDirection.ASC);

    final Page<Product> result = productService.search(company.getId(), true, request);

    assertEquals(
        List.of(target.getId()), result.getContent().stream().map(Product::getId).toList());
  }

  @Test
  void searchDefaultsToNonArchivedAndSupportsSortAndPaging() {
    final Product second =
        saveProduct("B", "Beta", "Brand", "Second", ProductStatus.ACTIVE, each, null, null);
    final Product first =
        saveProduct("A", "Alpha", "Brand", "First", ProductStatus.ACTIVE, each, null, null);
    saveProduct(
        "C",
        "Archived",
        "Brand",
        "Archived",
        ProductStatus.ACTIVE,
        each,
        null,
        LocalDateTime.now());

    final ProductListRequest request =
        new ProductListRequest(
            0, 1, "  ", " ", null, null, null, ProductSortField.SKU, ProductSortDirection.DESC);

    final Page<Product> result = productService.search(company.getId(), false, request);

    assertEquals(2, result.getTotalElements());
    assertEquals(2, result.getTotalPages());
    assertEquals(
        List.of(second.getId()), result.getContent().stream().map(Product::getId).toList());
    assertEquals(
        first.getId(),
        productService
            .search(
                company.getId(),
                false,
                new ProductListRequest(
                    0,
                    1,
                    null,
                    null,
                    null,
                    null,
                    null,
                    ProductSortField.SKU,
                    ProductSortDirection.ASC))
            .getContent()
            .getFirst()
            .getId());
  }

  private Product saveProduct(
      String sku,
      String name,
      String brand,
      String description,
      ProductStatus status,
      Unit mainUnit,
      Unit secondaryUnit,
      LocalDateTime deletedAt) {
    final Brand brandEntity =
        brand == null
            ? null
            : brandRepository
                .findByCompanyIdAndNameIgnoreCase(company.getId(), brand)
                .orElseGet(
                    () -> {
                      final Brand newBrand = new Brand();
                      newBrand.setCompany(company);
                      newBrand.setName(brand);
                      return brandRepository.save(newBrand);
                    });
    return productRepository.save(
        Product.builder()
            .company(company)
            .sku(sku)
            .name(name)
            .brand(brandEntity)
            .description(description)
            .status(status)
            .unit(mainUnit)
            .secondaryUnit(secondaryUnit)
            .secondaryUnitsPerMainUnit(secondaryUnit == null ? null : new BigDecimal("12"))
            .netPrice(BigDecimal.ONE)
            .vatRate(BigDecimal.ZERO)
            .deletedAt(deletedAt)
            .build());
  }

  private void assign(Product product, ProductCategory category) {
    assignmentRepository.save(
        ProductCategoryAssignment.builder()
            .id(new ProductCategoryAssignmentId(product.getId(), category.getId()))
            .product(product)
            .category(category)
            .build());
  }

  private static Unit unit(String code) {
    return Unit.builder().code(code).name(code).symbol(code).system(true).build();
  }
}
