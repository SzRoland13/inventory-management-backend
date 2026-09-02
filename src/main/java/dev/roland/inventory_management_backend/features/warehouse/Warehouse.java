package dev.roland.inventory_management_backend.features.warehouse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.warehouse_location.WarehouseLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a physical or logical storage location where inventory is held.
 *
 * <p>A warehouse references a {@link WarehouseLocation} and optionally the {@link User} who created
 * it. Its stock balances map provides a warehouse-side view of the one-to-many relationship between
 * warehouses and {@link StockBalance} rows; products are the other side of each balance.
 */
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

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updated_at;

  @OneToMany(mappedBy = "warehouse")
  @MapKey(name = "product")
  @Builder.Default
  private Map<Product, StockBalance> stockBalances = new HashMap<>();
}
