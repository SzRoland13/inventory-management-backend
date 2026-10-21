package dev.roland.inventory_management_backend.features.warehouse.dto;

import java.time.LocalDateTime;

import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import lombok.Builder;
import lombok.Getter;

/** Warehouse details returned by the warehouse API. */
@Getter
@Builder
public class WarehouseResponse {
  private Long id;
  private Long companyId;
  private String name;
  private String code;
  private WarehouseType type;
  private WarehouseStatus status;
  private WarehouseAddressDto address;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
