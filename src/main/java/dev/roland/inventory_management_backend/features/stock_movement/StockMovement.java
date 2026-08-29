package dev.roland.inventory_management_backend.features.stock_movement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import dev.roland.inventory_management_backend.common.persistance.IdInterface;
import dev.roland.inventory_management_backend.enums.StockMovementType;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an inventory quantity change recorded in the stock ledger.
 *
 * <p>A movement references the affected {@link Warehouse} and {@link Product}, and may reference
 * the originating {@link Document} and {@link DocumentLine}. It also records the movement type,
 * signed quantity change, timestamp, and optionally the {@link User} who created it. The movement
 * history is the audit source used to explain stock balances.
 */
@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_id")
  private Document document;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_line_id")
  private DocumentLine documentLine;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "warehouse_id", nullable = false)
  private Warehouse warehouse;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "quantity_change", nullable = false, precision = 15, scale = 2)
  private BigDecimal quantityChange;

  @Column(name = "movement_type", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private StockMovementType movementType;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id")
  private User createdByUser;
}
