package dev.roland.inventory_management_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.*;

import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sku", nullable = false, unique = true, length = 100)
  private String sku;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "unit", length = 50, nullable = false)
  private String unit;

  @Column(name = "net_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal netPrice;

  @Column(name = "vat_rate", nullable = false, precision = 5, scale = 3)
  private BigDecimal vatRate;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "product")
  @MapKey(name = "warehouse")
  private Map<Warehouse, StockBalance> stockBalances = new HashMap<>();
}
