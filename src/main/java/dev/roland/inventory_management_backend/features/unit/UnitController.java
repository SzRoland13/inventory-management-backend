package dev.roland.inventory_management_backend.features.unit;

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
import dev.roland.inventory_management_backend.features.product.dto.UnitRequest;
import dev.roland.inventory_management_backend.features.product.dto.UnitResponse;
import dev.roland.inventory_management_backend.features.product.message.ProductSettingsMessageKey;
import dev.roland.inventory_management_backend.features.unit.facade.UnitFacade;
import lombok.RequiredArgsConstructor;

/** Exposes product unit settings. */
@RestController
@RequestMapping(UnitController.BASE_ENDPOINT)
@RequiredArgsConstructor
public class UnitController {
  public static final String BASE_ENDPOINT = "api/v1/product-settings/units";
  public static final String ID_PATH = "/{id}";

  private final UnitFacade unitFacade;

  /**
   * Lists selectable units for the current company.
   *
   * @return selectable units
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<UnitResponse>>> list() {
    return ok(ProductSettingsMessageKey.UNITS_RETRIEVED, unitFacade.list());
  }

  /**
   * Creates a company unit.
   *
   * @param request unit data
   * @return created unit
   */
  @PostMapping
  @AdminOnly
  public ResponseEntity<ApiResponse<UnitResponse>> create(
      @Valid @RequestBody final UnitRequest request) {
    return ok(ProductSettingsMessageKey.UNIT_CREATED, unitFacade.create(request));
  }

  /**
   * Updates a company unit.
   *
   * @param id unit identifier
   * @param request updated unit data
   * @return updated unit
   */
  @PutMapping(ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<UnitResponse>> update(
      @PathVariable final Long id, @Valid @RequestBody final UnitRequest request) {
    return ok(ProductSettingsMessageKey.UNIT_UPDATED, unitFacade.update(id, request));
  }

  /**
   * Deletes an unused company unit.
   *
   * @param id unit identifier
   * @return successful response with no payload
   */
  @DeleteMapping(ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final Long id) {
    unitFacade.delete(id);
    return ok(ProductSettingsMessageKey.UNIT_DELETED, null);
  }

  private <T> ResponseEntity<ApiResponse<T>> ok(
      final ProductSettingsMessageKey messageKey, final T payload) {
    return ResponseEntity.ok(ApiResponse.success(messageKey, payload));
  }
}
