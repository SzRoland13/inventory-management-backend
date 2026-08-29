package dev.roland.inventory_management_backend.features.document_prefix;

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
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;

import dev.roland.inventory_management_backend.common.persistance.IdInterface;
import dev.roland.inventory_management_backend.enums.DocumentType;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document_sequence.DocumentSequence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Defines the textual prefix used when numbering one document type for a {@link Company}.
 *
 * <p>A prefix belongs to one company and document type. It is referenced by {@link
 * DocumentSequence}, which maintains the yearly counter used to generate document numbers.
 */
@Entity
@Table(
    name = "document_prefixes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "document_type", "prefix"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DocumentPrefix implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(name = "document_type", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private DocumentType documentType;

  @Column(name = "prefix", nullable = false, length = 20)
  private String prefix;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
