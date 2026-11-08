package dev.roland.inventory_management_backend.features.warehouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Fields used to create or update a warehouse. */
@Getter
@Setter
@NoArgsConstructor
public class WarehouseRequest {

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Size(max = 150, message = ValidationKeys.TOO_LONG)
  private String name;

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Size(max = 50, message = ValidationKeys.TOO_LONG)
  private String code;

  @NotNull(message = ValidationKeys.REQUIRED)
  private WarehouseType type;

  @Valid private WarehouseAddressDto address;
}
