package dev.roland.inventory_management_backend.features.brand;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.common.annotation.AdminOrManager;
import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.features.brand.dto.BrandRequest;
import dev.roland.inventory_management_backend.features.brand.dto.BrandResponse;
import dev.roland.inventory_management_backend.features.brand.facade.BrandFacade;
import dev.roland.inventory_management_backend.features.brand.message.BrandMessageKey;
import lombok.RequiredArgsConstructor;

/** Exposes company-owned brand search. */
@RestController
@RequestMapping(BrandController.BASE_ENDPOINT)
@RequiredArgsConstructor
public class BrandController {
  public static final String BASE_ENDPOINT = "api/v1/brands";
  public static final String ID_PATH = "/{id}";
  private final BrandFacade brandFacade;

  /**
   * Searches the current company's brands by name.
   *
   * @param query name substring
   * @return matching brands
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<BrandResponse>>> search(
      @RequestParam(defaultValue = "") final String query) {
    return ResponseEntity.ok(
        ApiResponse.success(BrandMessageKey.BRANDS_RETRIEVED, brandFacade.search(query)));
  }

  /**
   * Creates a brand for the current company.
   *
   * @param request brand data
   * @return created brand
   */
  @PostMapping
  @AdminOrManager
  public ResponseEntity<ApiResponse<BrandResponse>> create(
      @Valid @RequestBody final BrandRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(BrandMessageKey.BRAND_CREATED, brandFacade.create(request)));
  }

  /**
   * Updates a brand owned by the current company.
   *
   * @param id brand identifier
   * @param request updated brand data
   * @return updated brand
   */
  @PutMapping(ID_PATH)
  @AdminOrManager
  public ResponseEntity<ApiResponse<BrandResponse>> update(
      @PathVariable final Long id, @Valid @RequestBody final BrandRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(BrandMessageKey.BRAND_UPDATED, brandFacade.update(id, request)));
  }

  /**
   * Deletes a brand owned by the current company.
   *
   * @param id brand identifier
   * @return successful response with no payload
   */
  @DeleteMapping(ID_PATH)
  @AdminOrManager
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final Long id) {
    brandFacade.delete(id);
    return ResponseEntity.ok(ApiResponse.success(BrandMessageKey.BRAND_DELETED, null));
  }
}
