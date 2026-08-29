package dev.roland.inventory_management_backend.dto.company;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyMinimalResponse {
  private Long id;
  private String name;
  private Long logoId;
  private String logoUrl;
  private Instant logoUrlExpiry;
}
