package dev.roland.inventory_management_backend.features.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyBaseDataResponse {
  private Long id;
  private String name;
  private String description;
  private String email;
  private String phone;
  private String address;
  private String website;
}
