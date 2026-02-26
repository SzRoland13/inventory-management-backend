package dev.roland.inventory_management_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;

@Entity
@Table(
    name = "stock_balances",
    uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockBalance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "warehouse_id", nullable = false)
  private Warehouse warehouse;

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "quantity", precision = 15, scale = 2)
  private BigDecimal quantity = BigDecimal.ZERO;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
