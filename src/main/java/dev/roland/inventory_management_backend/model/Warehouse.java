package dev.roland.inventory_management_backend.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.*;

import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, unique = true, length = 150)
  private String name;

  @ManyToOne
  @JoinColumn(name = "location_id", nullable = false)
  private WarehouseLocation location;

  @ManyToOne
  @JoinColumn(name = "created_by_user_id")
  private User createdBy;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updated_at;

  @OneToMany(mappedBy = "warehouse")
  @MapKey(name = "product")
  private Map<Product, StockBalance> stockBalances = new HashMap<>();
}
