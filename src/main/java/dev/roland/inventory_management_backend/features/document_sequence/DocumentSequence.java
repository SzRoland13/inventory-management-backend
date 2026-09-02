package dev.roland.inventory_management_backend.features.document_sequence;

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
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.common.persistance.IdInterface;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Stores a yearly numbering counter for a company's document prefix.
 *
 * <p>Each sequence belongs to one {@link Company} and one {@link DocumentPrefix}. The unique
 * company-prefix-year combination ensures that document numbers can be generated consistently for
 * each business year. {@link Document} references the sequence used for its number.
 */
@Entity
@Table(
    name = "document_sequences",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"company_id", "document_prefix_id", "year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DocumentSequence implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "document_prefix_id", nullable = false)
  private DocumentPrefix documentPrefix;

  @Column(name = "year", nullable = false)
  private Integer year;

  @Column(name = "counter", nullable = false)
  private Long counter = 0L;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
