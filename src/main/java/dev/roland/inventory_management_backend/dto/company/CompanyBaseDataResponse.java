package dev.roland.inventory_management_backend.dto.company;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyBaseDataResponse {
  private long id;
  private String name;
  private String logoUrl;
  private Instant logoUrlExpiry;
  private boolean exists;
}
