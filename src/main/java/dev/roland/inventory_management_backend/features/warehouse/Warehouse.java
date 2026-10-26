package dev.roland.inventory_management_backend.features.warehouse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.common.persistance.IdInterface;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import dev.roland.inventory_management_backend.features.warehouse_address.WarehouseAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a physical or logical storage location where inventory is held.
 *
 * <p>A warehouse belongs to a {@link Company}, may have one {@link WarehouseAddress}, and may
 * reference the {@link User} who created it. Its stock balances map provides a warehouse-side view
 * of the one-to-many relationship between warehouses and {@link StockBalance} rows.
 */
@Entity
@Table(
    name = "warehouses",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"company_id", "name"}),
      @UniqueConstraint(columnNames = {"company_id", "code"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @ManyToOne(optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "address_id", unique = true)
  private WarehouseAddress address;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  @Builder.Default
  private WarehouseType type = WarehouseType.STANDARD;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  @Builder.Default
  private WarehouseStatus status = WarehouseStatus.ACTIVE;

  @ManyToOne
  @JoinColumn(name = "created_by_user_id")
  private User createdBy;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "warehouse")
  @MapKey(name = "product")
  @Builder.Default
  private Map<Product, StockBalance> stockBalances = new HashMap<>();
}
