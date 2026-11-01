package dev.roland.inventory_management_backend.features.document_line_allocation;

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

import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Allocates an operation line quantity to one source order line. */
@Entity
@Table(name = "document_line_allocations")
@Getter
@Setter
@NoArgsConstructor
public class DocumentLineAllocation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "fulfillment_line_id", nullable = false)
  private DocumentLine fulfillmentLine;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_order_line_id", nullable = false)
  private DocumentLine sourceOrderLine;

  @Column(nullable = false, precision = 24, scale = 8)
  private BigDecimal quantity;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
