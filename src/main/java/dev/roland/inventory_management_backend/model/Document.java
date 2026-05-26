package dev.roland.inventory_management_backend.model;

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
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.enums.DocumentCategory;
import dev.roland.inventory_management_backend.enums.DocumentStatus;
import dev.roland.inventory_management_backend.enums.DocumentType;
import dev.roland.inventory_management_backend.model.interfaces.IdInterface;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_warehouse_id")
  private Warehouse sourceWarehouse;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_warehouse_id")
  private Warehouse targetWarehouse;

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
}
