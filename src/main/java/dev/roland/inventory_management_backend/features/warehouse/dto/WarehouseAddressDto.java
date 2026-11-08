package dev.roland.inventory_management_backend.features.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.CountryCode;
import dev.roland.inventory_management_backend.common.validation.E164Phone;
import dev.roland.inventory_management_backend.common.validation.EmailAddress;
import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Optional postal and contact information for a warehouse. */
@Getter
@Setter
@NoArgsConstructor
@Builder
public class WarehouseAddressDto {

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Size(min = 2, max = 2, message = ValidationKeys.INVALID_VALUE)
  @CountryCode
  private String country;

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Size(max = 100, message = ValidationKeys.TOO_LONG)
  private String city;

  @NotBlank(message = ValidationKeys.REQUIRED)
  @Size(max = 255, message = ValidationKeys.TOO_LONG)
  private String addressLine1;

  @Size(max = 255, message = ValidationKeys.TOO_LONG)
  private String addressLine2;

  @Size(max = 50, message = ValidationKeys.TOO_LONG)
  @E164Phone
  private String phone;

  @EmailAddress
  @Size(max = 254, message = ValidationKeys.INVALID_EMAIL)
  private String email;

  /**
   * Creates an address from its postal and contact fields.
   *
   * @param country country code
   * @param city city
   * @param addressLine1 primary street address
   * @param addressLine2 secondary street address
   * @param phone contact phone
   * @param email contact email
   */
  public WarehouseAddressDto(
      final String country,
      final String city,
      final String addressLine1,
      final String addressLine2,
      final String phone,
      final String email) {
    this.country = country;
    this.city = city;
    this.addressLine1 = addressLine1;
    this.addressLine2 = addressLine2;
    this.phone = phone;
    this.email = email;
  }
}
