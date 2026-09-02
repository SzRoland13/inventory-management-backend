package dev.roland.inventory_management_backend.features.company.dto;

import java.time.Instant;

import dev.roland.inventory_management_backend.features.currency.dto.CurrencyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyExtendedResponse {
  private Long id;
  private String name;
  private Long logoId;
  private String logoUrl;
  private Instant logoUrlExpiry;

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
  private CurrencyResponse preferredCurrency;
}
