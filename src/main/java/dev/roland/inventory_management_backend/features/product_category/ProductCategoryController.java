package dev.roland.inventory_management_backend.features.product_category;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.common.annotation.AdminOnly;
import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.features.product.dto.CategoryRequest;
import dev.roland.inventory_management_backend.features.product.dto.CategoryResponse;
import dev.roland.inventory_management_backend.features.product.message.ProductSettingsMessageKey;
import dev.roland.inventory_management_backend.features.product_category.facade.ProductCategoryFacade;
import lombok.RequiredArgsConstructor;

/** Exposes product category settings. */
@RestController
@RequestMapping(ProductCategoryController.BASE_ENDPOINT)
@RequiredArgsConstructor
public class ProductCategoryController {
  public static final String BASE_ENDPOINT = "api/v1/product-settings/categories";
  public static final String ID_PATH = "/{id}";

  private final ProductCategoryFacade productCategoryFacade;

  /**
   * Lists categories owned by the current company.
   *
   * @return company categories
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> list() {
    return ok(ProductSettingsMessageKey.CATEGORIES_RETRIEVED, productCategoryFacade.list());
  }

  /**
   * Creates a company category.
   *
   * @param request category data
   * @return created category
   */
  @PostMapping
  @AdminOnly
  public ResponseEntity<ApiResponse<CategoryResponse>> create(
      @Valid @RequestBody final CategoryRequest request) {
    return ok(ProductSettingsMessageKey.CATEGORY_CREATED, productCategoryFacade.create(request));
  }

  /**
   * Updates a company category.
   *
   * @param id category identifier
   * @param request updated category data
   * @return updated category
   */
  @PutMapping(ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<CategoryResponse>> update(
      @PathVariable final Long id, @Valid @RequestBody final CategoryRequest request) {
    return ok(
        ProductSettingsMessageKey.CATEGORY_UPDATED, productCategoryFacade.update(id, request));
  }

  /**
   * Deletes a company category.
   *
   * @param id category identifier
   * @return successful response with no payload
   */
  @DeleteMapping(ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final Long id) {
    productCategoryFacade.delete(id);
    return ok(ProductSettingsMessageKey.CATEGORY_DELETED, null);
  }

  private <T> ResponseEntity<ApiResponse<T>> ok(
      final ProductSettingsMessageKey messageKey, final T payload) {
    return ResponseEntity.ok(ApiResponse.success(messageKey, payload));
  }
}
