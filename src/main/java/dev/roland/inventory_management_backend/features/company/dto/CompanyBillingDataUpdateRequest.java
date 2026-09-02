package dev.roland.inventory_management_backend.features.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyBillingDataUpdateRequest {
  private String taxNumber;
  private String vatNumber;
  private String registrationNumber;
  private String bankAccount;
  private String iban;
}
