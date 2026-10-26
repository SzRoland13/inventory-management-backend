package dev.roland.inventory_management_backend.features.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

  @NotBlank
  @Size(max = 2)
  private String country;

  @NotBlank
  @Size(max = 100)
  private String city;

  @NotBlank
  @Size(max = 255)
  private String addressLine1;

  @Size(max = 255)
  private String addressLine2;

  @Size(max = 50)
  private String phone;

  @Size(max = 255)
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
