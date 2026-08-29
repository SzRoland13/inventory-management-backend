package dev.roland.inventory_management_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.model.interfaces.IdInterface;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents one product line belonging to a {@link Document}.
 *
 * <p>The line stores ordered quantity, fulfilled quantity, pricing, currency, and snapshots of
 * product information so a document can retain historical values after the product changes. It
 * references exactly one document and one {@link Product}; completed lines may also be referenced
 * by {@link StockMovement} records.
 */
@Entity
@Table(name = "document_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentLine implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "document_id", nullable = false)
  private Document document;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal quantity;

  @Column(name = "fulfilled_quantity", nullable = false, precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal fulfilledQuantity = BigDecimal.ZERO;

  @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalPrice;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "currency_id")
  private Currency currency;

  @Column(name = "product_name_snapshot")
  private String productNameSnapshot;

  @Column(name = "product_sku_snapshot", length = 100)
  private String productSkuSnapshot;

  @Column(name = "vat_rate_snapshot", precision = 5, scale = 3)
  private BigDecimal vatRateSnapshot;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
