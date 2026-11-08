package dev.roland.inventory_management_backend.features.company.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.E164Phone;
import dev.roland.inventory_management_backend.common.validation.EmailAddress;
import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Carries input data for company Base Data Update in the company profile API. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyBaseDataUpdateRequest {
  @Size(max = 255, message = ValidationKeys.TOO_LONG)
  private String name;

  @Size(max = 2000, message = ValidationKeys.TOO_LONG)
  private String description;

  @EmailAddress
  @Size(max = 254, message = ValidationKeys.INVALID_EMAIL)
  private String email;

  @Size(max = 50, message = ValidationKeys.TOO_LONG)
  @E164Phone
  private String phone;

  @Size(max = 500, message = ValidationKeys.TOO_LONG)
  private String address;

  @Size(max = 2048, message = ValidationKeys.TOO_LONG)
  @Pattern(regexp = "^$|^https?://\\S+$", message = ValidationKeys.INVALID_VALUE)
  private String website;
}
