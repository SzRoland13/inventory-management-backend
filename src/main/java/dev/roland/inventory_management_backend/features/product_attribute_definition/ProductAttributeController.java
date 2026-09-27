package dev.roland.inventory_management_backend.features.product_attribute_definition;

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
import dev.roland.inventory_management_backend.features.product.message.ProductSettingsMessageKey;
import dev.roland.inventory_management_backend.features.product_attribute_definition.facade.ProductAttributeFacade;
import lombok.RequiredArgsConstructor;

/** Exposes product attribute definitions and their options. */
@RestController
@RequestMapping(ProductAttributeController.BASE_ENDPOINT)
@RequiredArgsConstructor
public class ProductAttributeController {
  public static final String BASE_ENDPOINT = "api/v1/product-settings/attributes";
  public static final String ID_PATH = "/{id}";
  public static final String OPTIONS_PATH = "/{definitionId}/options";
  public static final String OPTION_ID_PATH = OPTIONS_PATH + "/{optionId}";

  private final ProductAttributeFacade productAttributeFacade;

  /**
   * Lists company attribute definitions and their options.
   *
   * @return attribute definitions
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<AttributeDefinitionResponse>>> list() {
    return ok(ProductSettingsMessageKey.ATTRIBUTES_RETRIEVED, productAttributeFacade.list());
  }

  /**
   * Creates an attribute definition.
   *
   * @param request definition data
   * @return created definition
   */
  @PostMapping
  @AdminOnly
  public ResponseEntity<ApiResponse<AttributeDefinitionResponse>> create(
      @Valid @RequestBody final AttributeDefinitionRequest request) {
    return ok(ProductSettingsMessageKey.ATTRIBUTE_CREATED, productAttributeFacade.create(request));
  }

  /**
   * Updates an attribute definition.
   *
   * @param id definition identifier
   * @param request updated definition data
   * @return updated definition
   */
  @PutMapping(ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<AttributeDefinitionResponse>> update(
      @PathVariable final Long id, @Valid @RequestBody final AttributeDefinitionRequest request) {
    return ok(
        ProductSettingsMessageKey.ATTRIBUTE_UPDATED, productAttributeFacade.update(id, request));
  }

  /**
   * Deletes an attribute definition.
   *
   * @param id definition identifier
   * @return successful response with no payload
   */
  @DeleteMapping(ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final Long id) {
    productAttributeFacade.delete(id);
    return ok(ProductSettingsMessageKey.ATTRIBUTE_DELETED, null);
  }

  /**
   * Creates an option for an attribute definition.
   *
   * @param definitionId definition identifier
   * @param request option data
   * @return created option
   */
  @PostMapping(OPTIONS_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<AttributeOptionResponse>> createOption(
      @PathVariable final Long definitionId,
      @Valid @RequestBody final AttributeOptionRequest request) {
    return ok(
        ProductSettingsMessageKey.ATTRIBUTE_OPTION_CREATED,
        productAttributeFacade.createOption(definitionId, request));
  }

  /**
   * Updates an option belonging to an attribute definition.
   *
   * @param definitionId definition identifier
   * @param optionId option identifier
   * @param request updated option data
   * @return updated option
   */
  @PutMapping(OPTION_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<AttributeOptionResponse>> updateOption(
      @PathVariable final Long definitionId,
      @PathVariable final Long optionId,
      @Valid @RequestBody final AttributeOptionRequest request) {
    return ok(
        ProductSettingsMessageKey.ATTRIBUTE_OPTION_UPDATED,
        productAttributeFacade.updateOption(definitionId, optionId, request));
  }

  /**
   * Deletes an option belonging to an attribute definition.
   *
   * @param definitionId definition identifier
   * @param optionId option identifier
   * @return successful response with no payload
   */
  @DeleteMapping(OPTION_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<Void>> deleteOption(
      @PathVariable final Long definitionId, @PathVariable final Long optionId) {
    productAttributeFacade.deleteOption(definitionId, optionId);
    return ok(ProductSettingsMessageKey.ATTRIBUTE_OPTION_DELETED, null);
  }

  private <T> ResponseEntity<ApiResponse<T>> ok(
      final ProductSettingsMessageKey messageKey, final T payload) {
    return ResponseEntity.ok(ApiResponse.success(messageKey, payload));
  }
}
