package dev.roland.inventory_management_backend.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyBillingDataResponse {
  private Long id;
  private String taxNumber;
  private String vatNumber;
  private String registrationNumber;
  private String bankAccount;
  private String iban;
}
