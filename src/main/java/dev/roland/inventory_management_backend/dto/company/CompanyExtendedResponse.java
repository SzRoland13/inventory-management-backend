package dev.roland.inventory_management_backend.dto.company;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyExtendedResponse {
  private long id;
  private String name;
  private String logoUrl;
  private Instant logoUrlExpiry;
  private String description;
  private String email;
  private String phone;
  private String address;
  private String website;
  private boolean exists;
}
