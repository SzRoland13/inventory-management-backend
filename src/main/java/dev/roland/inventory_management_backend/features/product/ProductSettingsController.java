package dev.roland.inventory_management_backend.features.product;

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
import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeDefinitionResponse;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionRequest;
import dev.roland.inventory_management_backend.features.product.dto.AttributeOptionResponse;
import dev.roland.inventory_management_backend.features.product.dto.CategoryRequest;
import dev.roland.inventory_management_backend.features.product.dto.CategoryResponse;
import dev.roland.inventory_management_backend.features.product.dto.UnitRequest;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.product.facade.ProductSettingsFacade;
import dev.roland.inventory_management_backend.features.product.message.ProductSettingsMessageKey;
import lombok.RequiredArgsConstructor;

/** Exposes units, categories, and attribute definitions for product settings. */
@RestController
@RequestMapping(ProductSettingsController.BASE_ENDPOINT)
@RequiredArgsConstructor
public class ProductSettingsController {
  public static final String BASE_ENDPOINT = "api/v1/product-settings";
  public static final String ID_PATH = "/{id}";
  public static final String UNITS_PATH = "/units";
  public static final String CATEGORIES_PATH = "/categories";
  public static final String ATTRIBUTES_PATH = "/attributes";
  public static final String UNIT_ID_PATH = UNITS_PATH + ID_PATH;
  public static final String CATEGORY_ID_PATH = CATEGORIES_PATH + ID_PATH;
  public static final String ATTRIBUTE_ID_PATH = ATTRIBUTES_PATH + ID_PATH;
  public static final String ATTRIBUTE_OPTIONS_PATH = ATTRIBUTES_PATH + "/{definitionId}/options";
  public static final String ATTRIBUTE_OPTION_ID_PATH = ATTRIBUTE_OPTIONS_PATH + "/{optionId}";

  private final ProductSettingsFacade productSettingsFacade;

  /**
   * Lists the requested resources.
   *
   * @return the matching resources
   */
  @GetMapping(UNITS_PATH)
  public ResponseEntity<ApiResponse<List<UnitResponse>>> listUnits() {
    return ok(ProductSettingsMessageKey.UNITS_RETRIEVED, productSettingsFacade.listUnits());
  }

  /**
   * Creates the requested resource.
   *
   * @param request the validated request
   * @return the API response
   */
  @PostMapping(UNITS_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<UnitResponse>> createUnit(
      @Valid @RequestBody final UnitRequest request) {
    return ok(ProductSettingsMessageKey.UNIT_CREATED, productSettingsFacade.createUnit(request));
  }

  /**
   * Updates the requested resource.
   *
   * @param id the resource identifier
   * @param request the validated request
   * @return the API response
   */
  @PutMapping(UNIT_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<UnitResponse>> updateUnit(
      @PathVariable final Long id, @Valid @RequestBody final UnitRequest request) {
    return ok(
        ProductSettingsMessageKey.UNIT_UPDATED, productSettingsFacade.updateUnit(id, request));
  }

  /**
   * Deletes the matching record or records.
   *
   * @param id the resource identifier
   * @return the API response
   */
  @DeleteMapping(UNIT_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable final Long id) {
    productSettingsFacade.deleteUnit(id);
    return ok(ProductSettingsMessageKey.UNIT_DELETED, null);
  }

  /**
   * Lists the requested resources.
   *
   * @return the matching resources
   */
  @GetMapping(CATEGORIES_PATH)
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories() {
    return ok(
        ProductSettingsMessageKey.CATEGORIES_RETRIEVED, productSettingsFacade.listCategories());
  }

  /**
   * Creates the requested resource.
   *
   * @param request the validated request
   * @return the API response
   */
  @PostMapping(CATEGORIES_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
      @Valid @RequestBody final CategoryRequest request) {
    return ok(
        ProductSettingsMessageKey.CATEGORY_CREATED, productSettingsFacade.createCategory(request));
  }

  /**
   * Updates the requested resource.
   *
   * @param id the resource identifier
   * @param request the validated request
   * @return the API response
   */
  @PutMapping(CATEGORY_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
      @PathVariable final Long id, @Valid @RequestBody final CategoryRequest request) {
    return ok(
        ProductSettingsMessageKey.CATEGORY_UPDATED,
        productSettingsFacade.updateCategory(id, request));
  }

  /**
   * Deletes the matching record or records.
   *
   * @param id the resource identifier
   * @return the API response
   */
  @DeleteMapping(CATEGORY_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable final Long id) {
    productSettingsFacade.deleteCategory(id);
    return ok(ProductSettingsMessageKey.CATEGORY_DELETED, null);
  }

  /**
   * Lists the requested resources.
   *
   * @return the matching resources
   */
  @GetMapping(ATTRIBUTES_PATH)
  public ResponseEntity<ApiResponse<List<AttributeDefinitionResponse>>> listDefinitions() {
    return ok(
        ProductSettingsMessageKey.ATTRIBUTES_RETRIEVED, productSettingsFacade.listDefinitions());
  }

  /**
   * Creates the requested resource.
   *
   * @param request the validated request
   * @return the API response
   */
  @PostMapping(ATTRIBUTES_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<AttributeDefinitionResponse>> createDefinition(
      @Valid @RequestBody final AttributeDefinitionRequest request) {
    return ok(
        ProductSettingsMessageKey.ATTRIBUTE_CREATED,
        productSettingsFacade.createDefinition(request));
  }

  /**
   * Updates the requested resource.
   *
   * @param id the resource identifier
   * @param request the validated request
   * @return the API response
   */
  @PutMapping(ATTRIBUTE_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<AttributeDefinitionResponse>> updateDefinition(
      @PathVariable final Long id, @Valid @RequestBody final AttributeDefinitionRequest request) {
    return ok(
        ProductSettingsMessageKey.ATTRIBUTE_UPDATED,
        productSettingsFacade.updateDefinition(id, request));
  }

  /**
   * Deletes the matching record or records.
   *
   * @param id the resource identifier
   * @return the API response
   */
  @DeleteMapping(ATTRIBUTE_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<Void>> deleteDefinition(@PathVariable final Long id) {
    productSettingsFacade.deleteDefinition(id);
    return ok(ProductSettingsMessageKey.ATTRIBUTE_DELETED, null);
  }

  /**
   * Creates the requested resource.
   *
   * @param definitionId the attribute definition identifier
   * @param request the validated request
   * @return the API response
   */
  @PostMapping(ATTRIBUTE_OPTIONS_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<AttributeOptionResponse>> createOption(
      @PathVariable final Long definitionId,
      @Valid @RequestBody final AttributeOptionRequest request) {
    return ok(
        ProductSettingsMessageKey.ATTRIBUTE_OPTION_CREATED,
        productSettingsFacade.createOption(definitionId, request));
  }

  /**
   * Updates the requested resource.
   *
   * @param definitionId the attribute definition identifier
   * @param optionId the attribute option identifier
   * @param request the validated request
   * @return the API response
   */
  @PutMapping(ATTRIBUTE_OPTION_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<AttributeOptionResponse>> updateOption(
      @PathVariable final Long definitionId,
      @PathVariable final Long optionId,
      @Valid @RequestBody final AttributeOptionRequest request) {
    return ok(
        ProductSettingsMessageKey.ATTRIBUTE_OPTION_UPDATED,
        productSettingsFacade.updateOption(definitionId, optionId, request));
  }

  /**
   * Deletes the matching record or records.
   *
   * @param definitionId the attribute definition identifier
   * @param optionId the attribute option identifier
   * @return the API response
   */
  @DeleteMapping(ATTRIBUTE_OPTION_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<Void>> deleteOption(
      @PathVariable final Long definitionId, @PathVariable final Long optionId) {
    productSettingsFacade.deleteOption(definitionId, optionId);
    return ok(ProductSettingsMessageKey.ATTRIBUTE_OPTION_DELETED, null);
  }

  private <T> ResponseEntity<ApiResponse<T>> ok(
      final ProductSettingsMessageKey messageKey, final T payload) {
    return ResponseEntity.ok(ApiResponse.success(messageKey, payload));
  }
}
