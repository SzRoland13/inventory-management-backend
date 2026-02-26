package dev.roland.inventory_management_backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;

@Entity
@Table(name = "warehouse_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseLocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email")
  private String email;

  @Column(name = "country", length = 2, nullable = false)
  private String country;

  @Column(name = "city", length = 100, nullable = false)
  private String city;

  @Column(name = "phone", length = 50)
  private String phone;

  @Column(name = "address_line1", nullable = false)
  private String addressLine1;

  @Column(name = "address_line2")
  private String addressLine2;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updated_at;

  @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Warehouse> warehouses = new ArrayList<>();
}
