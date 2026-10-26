package dev.roland.inventory_management_backend.features.warehouse;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.common.annotation.AdminOnly;
import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseRequest;
import dev.roland.inventory_management_backend.features.warehouse.dto.WarehouseResponse;
import dev.roland.inventory_management_backend.features.warehouse.facade.WarehouseFacade;
import dev.roland.inventory_management_backend.features.warehouse.message.WarehouseMessageKey;
import lombok.RequiredArgsConstructor;

/** Exposes company-scoped warehouse configuration endpoints. */
@RestController
@RequestMapping(WarehouseController.BASE_PATH)
@RequiredArgsConstructor
public class WarehouseController {
  public static final String BASE_PATH = "api/v1/warehouses";
  public static final String ARCHIVE_PATH = "/{id}/archive";
  public static final String RESTORE_PATH = "/{id}/restore";

  private final WarehouseFacade warehouseFacade;

  /**
   * Returns the warehouses belonging to the current company.
   *
   * @return company warehouses
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAll() {
    return ResponseEntity.ok(
        ApiResponse.success(
            WarehouseMessageKey.WAREHOUSES_RETRIEVED, warehouseFacade.findAllForCurrentCompany()));
  }

  /**
   * Creates a warehouse for the current company.
   *
   * @param request warehouse fields to create
   * @return created warehouse
   */
  @PostMapping
  @AdminOnly
  public ResponseEntity<ApiResponse<WarehouseResponse>> create(
      @Valid @RequestBody final WarehouseRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                WarehouseMessageKey.WAREHOUSE_CREATED, warehouseFacade.create(request)));
  }

  /**
   * Updates a warehouse owned by the current company.
   *
   * @param id warehouse identifier
   * @param request replacement warehouse fields
   * @return updated warehouse
   */
  @PutMapping("/{id}")
  @AdminOnly
  public ResponseEntity<ApiResponse<WarehouseResponse>> update(
      @PathVariable final Long id, @Valid @RequestBody final WarehouseRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            WarehouseMessageKey.WAREHOUSE_UPDATED, warehouseFacade.update(id, request)));
  }

  /**
   * Archives an empty warehouse.
   *
   * @param id warehouse identifier
   * @return archived warehouse
   */
  @PostMapping(ARCHIVE_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<WarehouseResponse>> archive(@PathVariable final Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(WarehouseMessageKey.WAREHOUSE_ARCHIVED, warehouseFacade.archive(id)));
  }

  /**
   * Restores an archived warehouse.
   *
   * @param id warehouse identifier
   * @return restored warehouse
   */
  @PostMapping(RESTORE_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<WarehouseResponse>> restore(@PathVariable final Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(WarehouseMessageKey.WAREHOUSE_RESTORED, warehouseFacade.restore(id)));
  }
}
