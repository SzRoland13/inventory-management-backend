package dev.roland.inventory_management_backend.features.document_line;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.persistance.IdInterface;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document_line.message.DocumentLineMessageKey;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.unit.Unit;
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

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "unit_id_snapshot", nullable = false)
  private Unit unitSnapshot;

  @Column(name = "unit_code_snapshot", nullable = false, length = 50)
  private String unitCodeSnapshot;

  @Column(name = "unit_name_snapshot", nullable = false, length = 100)
  private String unitNameSnapshot;

  @Column(name = "conversion_factor_snapshot", nullable = false, precision = 24, scale = 8)
  private BigDecimal conversionFactorSnapshot;

  @Column(nullable = false, precision = 24, scale = 8)
  private BigDecimal quantity;

  @Column(name = "fulfilled_quantity", nullable = false, precision = 24, scale = 8)
  @Builder.Default
  private BigDecimal fulfilledQuantity = BigDecimal.ZERO;

  @Column(name = "unit_price", nullable = false, precision = 24, scale = 8)
  private BigDecimal unitPrice;

  @Column(name = "total_price", nullable = false, precision = 24, scale = 8)
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

  /**
   * Captures the selected unit label and factor when a new document line is first persisted.
   *
   * @throws ApiException if the product or unit configuration is invalid
   */
  @PrePersist
  void initializeUnitSnapshot() {
    if (product == null) {
      throw new ApiException(DocumentLineMessageKey.PRODUCT_REQUIRED);
    }
    if (unitSnapshot == null) {
      unitSnapshot = product.getUnit();
    }
    if (unitSnapshot == null) {
      throw new ApiException(DocumentLineMessageKey.UNIT_REQUIRED);
    }
    if (unitCodeSnapshot == null) {
      unitCodeSnapshot = unitSnapshot.getCode();
    }
    if (unitNameSnapshot == null) {
      unitNameSnapshot = unitSnapshot.getName();
    }
    final BigDecimal expectedFactor = getExpectedFactor();
    if (conversionFactorSnapshot == null) {
      conversionFactorSnapshot = expectedFactor;
    } else if (conversionFactorSnapshot.compareTo(expectedFactor) != 0) {
      throw new ApiException(DocumentLineMessageKey.INVALID_CONVERSION_FACTOR);
    }
    if (conversionFactorSnapshot.signum() <= 0) {
      throw new ApiException(DocumentLineMessageKey.INVALID_CONVERSION_FACTOR);
    }
    if (productNameSnapshot == null) {
      productNameSnapshot = product.getName();
    }
    if (productSkuSnapshot == null) {
      productSkuSnapshot = product.getSku();
    }
    if (vatRateSnapshot == null) {
      vatRateSnapshot = product.getVatRate();
    }
  }

  private BigDecimal getExpectedFactor() {
    final boolean mainUnit = unitSnapshot.getId().equals(product.getUnit().getId());
    final boolean secondaryUnit =
        product.getSecondaryUnit() != null
            && unitSnapshot.getId().equals(product.getSecondaryUnit().getId());
    if (!mainUnit && !secondaryUnit) {
      throw new ApiException(DocumentLineMessageKey.INVALID_UNIT);
    }
    return mainUnit && product.getSecondaryUnit() != null
        ? product.getSecondaryUnitsPerMainUnit()
        : BigDecimal.ONE;
  }
}
