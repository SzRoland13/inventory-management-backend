package dev.roland.inventory_management_backend.features.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

import dev.roland.inventory_management_backend.common.persistance.IdInterface;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an item managed in the product catalog and inventory system.
 *
 * <p>A product references a mandatory {@link Unit}, and may reference a {@link Currency} and {@link
 * Company}. It is referenced by document lines, warehouse-specific {@link StockBalance} rows,
 * {@link StockMovement} rows, product attributes, and product-category assignments.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sku", nullable = false, unique = true, length = 100)
  private String sku;

  @Column(name = "ean", length = 20)
  private String ean;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "brand", length = 100)
  private String brand;

  @Column(name = "status", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private ProductStatus status = ProductStatus.ACTIVE;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "unit_id", nullable = false)
  private Unit unit;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "currency_id")
  private Currency currency;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @Column(name = "net_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal netPrice;

  @Column(name = "cost_price", precision = 15, scale = 2)
  private BigDecimal costPrice;

  @Column(name = "vat_rate", nullable = false, precision = 5, scale = 3)
  private BigDecimal vatRate;

  @Column(name = "weight", precision = 15, scale = 3)
  private BigDecimal weight;

  @Column(name = "width", precision = 15, scale = 3)
  private BigDecimal width;

  @Column(name = "height", precision = 15, scale = 3)
  private BigDecimal height;

  @Column(name = "depth", precision = 15, scale = 3)
  private BigDecimal depth;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "product")
  @MapKey(name = "warehouse")
  @Builder.Default
  private Map<Warehouse, StockBalance> stockBalances = new HashMap<>();
}
