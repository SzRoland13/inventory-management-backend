package dev.roland.inventory_management_backend.features.product;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.common.annotation.AdminOrManager;
import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductListRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductPageResponse;
import dev.roland.inventory_management_backend.features.product.dto.ProductRequest;
import dev.roland.inventory_management_backend.features.product.dto.ProductResponse;
import dev.roland.inventory_management_backend.features.product.facade.ProductManagementFacade;
import dev.roland.inventory_management_backend.features.product.message.ProductMessageKey;
import lombok.RequiredArgsConstructor;

/** Exposes product catalog endpoints. */
@RestController
@RequestMapping(ProductController.BASE_ENDPOINT)
@RequiredArgsConstructor
public class ProductController {
  public static final String BASE_ENDPOINT = "api/v1/products";
  public static final String ID_PATH = "/{id}";
  public static final String ARCHIVED_PATH = "/archived";
  public static final String RESTORE_PATH = ID_PATH + "/restore";

  private final ProductManagementFacade productManagementFacade;

  @GetMapping
  public ResponseEntity<ApiResponse<ProductPageResponse<ProductResponse>>> list(
      @Valid @ModelAttribute final ProductListRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            ProductMessageKey.PRODUCTS_RETRIEVED, productManagementFacade.list(false, request)));
  }

  @GetMapping(ID_PATH)
  public ResponseEntity<ApiResponse<ProductResponse>> get(@PathVariable final Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(
            ProductMessageKey.PRODUCT_RETRIEVED, productManagementFacade.get(id, false)));
  }

  @PostMapping
  @AdminOrManager
  public ResponseEntity<ApiResponse<ProductResponse>> create(
      @Valid @RequestBody final ProductRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            ProductMessageKey.PRODUCT_CREATED, productManagementFacade.create(request)));
  }

  @PutMapping(ID_PATH)
  @AdminOrManager
  public ResponseEntity<ApiResponse<ProductResponse>> update(
      @PathVariable final Long id, @Valid @RequestBody final ProductRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            ProductMessageKey.PRODUCT_UPDATED, productManagementFacade.update(id, request)));
  }

  @DeleteMapping(ID_PATH)
  @AdminOrManager
  public ResponseEntity<ApiResponse<Void>> archive(@PathVariable final Long id) {
    productManagementFacade.archive(id);
    return ResponseEntity.ok(ApiResponse.success(ProductMessageKey.PRODUCT_ARCHIVED, null));
  }

  @PostMapping(RESTORE_PATH)
  @AdminOrManager
  public ResponseEntity<ApiResponse<ProductResponse>> restore(@PathVariable final Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(
            ProductMessageKey.PRODUCT_RESTORED, productManagementFacade.restore(id)));
  }

  @GetMapping(ARCHIVED_PATH)
  @AdminOrManager
  public ResponseEntity<ApiResponse<ProductPageResponse<ProductResponse>>> listArchived(
      @Valid @ModelAttribute final ProductListRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            ProductMessageKey.ARCHIVED_PRODUCTS_RETRIEVED,
            productManagementFacade.list(true, request)));
  }
}
