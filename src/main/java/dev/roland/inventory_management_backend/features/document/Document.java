package dev.roland.inventory_management_backend.features.document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.common.persistance.IdInterface;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.document.enumeration.CommercialApprovalStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentCategory;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDirection;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDisposition;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;
import dev.roland.inventory_management_backend.features.document_sequence.DocumentSequence;
import dev.roland.inventory_management_backend.features.partner.Partner;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a business document in a procurement, sales, or internal warehouse workflow.
 *
 * <p>A document may reference a {@link DocumentSequence}, {@link Partner}, source and target {@link
 * Warehouse} records, the creating {@link User}, and a {@link Currency}. Its lines are stored in
 * {@link DocumentLine} and cascade with the document. Documents can be linked to other documents
 * through {@link DocumentRelation} and may generate {@link StockMovement} records when completed.
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @Column(name = "document_number", length = 100)
  private String documentNumber;

  @Column(name = "category", length = 50)
  @Enumerated(EnumType.STRING)
  private DocumentCategory category;

  @Column(nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private DocumentType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_sequence_id")
  private DocumentSequence documentSequence;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "partner_id")
  private Partner partner;

  @Column(name = "partner_name_snapshot", length = 255)
  private String partnerNameSnapshot;

  @Column(name = "partner_tax_number_snapshot", length = 50)
  private String partnerTaxNumberSnapshot;

  @Column(name = "partner_address_snapshot", columnDefinition = "TEXT")
  private String partnerAddressSnapshot;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_warehouse_id")
  private Warehouse sourceWarehouse;

  @Column(name = "source_warehouse_code_snapshot", length = 50)
  private String sourceWarehouseCodeSnapshot;

  @Column(name = "source_warehouse_name_snapshot", length = 255)
  private String sourceWarehouseNameSnapshot;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_warehouse_id")
  private Warehouse targetWarehouse;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transit_warehouse_id")
  private Warehouse transitWarehouse;

  @Column(name = "target_warehouse_code_snapshot", length = 50)
  private String targetWarehouseCodeSnapshot;

  @Column(name = "target_warehouse_name_snapshot", length = 255)
  private String targetWarehouseNameSnapshot;

  @Column(name = "transit_warehouse_code_snapshot", length = 50)
  private String transitWarehouseCodeSnapshot;

  @Column(name = "transit_warehouse_name_snapshot", length = 255)
  private String transitWarehouseNameSnapshot;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id")
  private User createdByUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "currency_id")
  private Currency currency;

  @Column(name = "exchange_rate", precision = 15, scale = 6)
  private BigDecimal exchangeRate;

  @Column(name = "status", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private DocumentStatus status;

  @Column(name = "reference_number", length = 100)
  private String referenceNumber;

  @Column(name = "notes")
  private String notes;

  @Column(name = "internal_notes")
  private String internalNotes;

  @Column(name = "workflow_reason", columnDefinition = "TEXT")
  private String workflowReason;

  @Enumerated(EnumType.STRING)
  @Column(name = "workflow_direction", length = 40)
  private ReturnDirection workflowDirection;

  @Enumerated(EnumType.STRING)
  @Column(name = "workflow_disposition", length = 40)
  private ReturnDisposition workflowDisposition;

  @Column(name = "counted_at")
  private LocalDateTime countedAt;

  @Column(name = "calculation_version", length = 100)
  private String calculationVersion;

  @Column(name = "non_billable", nullable = false)
  private boolean nonBillable;

  @Column(name = "payment_terms_snapshot", length = 500)
  private String paymentTermsSnapshot;

  @Column(name = "approval_required", nullable = false)
  private boolean approvalRequired;

  @Enumerated(EnumType.STRING)
  @Column(name = "approval_status", length = 30)
  private CommercialApprovalStatus approvalStatus;

  @Column(name = "approval_comment", columnDefinition = "TEXT")
  private String approvalComment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "approved_by_user_id")
  private User approvedByUser;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @Column(name = "financial_settlement_required", nullable = false)
  private boolean financialSettlementRequired;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "financial_settlement_owner_id")
  private User financialSettlementOwner;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "document_date", nullable = false)
  private LocalDate documentDate;

  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DocumentLine> lines;

  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DocumentExchangeRate> exchangeRates;

  /** Copies the current warehouse codes into the document snapshots. */
  @PrePersist
  void initializeWarehouseCodeSnapshots() {
    if (sourceWarehouse != null && sourceWarehouseCodeSnapshot == null) {
      sourceWarehouseCodeSnapshot = sourceWarehouse.getCode();
    }
    if (sourceWarehouse != null && sourceWarehouseNameSnapshot == null) {
      sourceWarehouseNameSnapshot = sourceWarehouse.getName();
    }
    if (targetWarehouse != null && targetWarehouseCodeSnapshot == null) {
      targetWarehouseCodeSnapshot = targetWarehouse.getCode();
    }
    if (targetWarehouse != null && targetWarehouseNameSnapshot == null) {
      targetWarehouseNameSnapshot = targetWarehouse.getName();
    }
    if (transitWarehouse != null && transitWarehouseCodeSnapshot == null) {
      transitWarehouseCodeSnapshot = transitWarehouse.getCode();
    }
    if (transitWarehouse != null && transitWarehouseNameSnapshot == null) {
      transitWarehouseNameSnapshot = transitWarehouse.getName();
    }
  }
}
