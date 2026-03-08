package dev.roland.inventory_management_backend.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyUpdateRequest {
  private String name;
  private String description;
  private String email;
  private String phone;
  private String address;
  private String website;

  private String taxNumber;
  private String vatNumber;
  private String registrationNumber;
  private String bankAccount;
  private String iban;
  private String invoicePrefix;
}
