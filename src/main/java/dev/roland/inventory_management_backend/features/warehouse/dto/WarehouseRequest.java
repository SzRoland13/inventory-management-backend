package dev.roland.inventory_management_backend.features.warehouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Fields used to create or update a warehouse. */
@Getter
@Setter
@NoArgsConstructor
public class WarehouseRequest {

  @NotBlank
  @Size(max = 150)
  private String name;

  @NotBlank
  @Size(max = 50)
  private String code;

  @NotNull private WarehouseType type;

  @Valid private WarehouseAddressDto address;
}
