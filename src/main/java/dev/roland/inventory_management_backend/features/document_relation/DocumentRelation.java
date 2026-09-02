package dev.roland.inventory_management_backend.features.document_relation;

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
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Links two documents in a business-process chain.
 *
 * <p>The source and target columns both reference {@link Document}; the relation type describes
 * whether the target was created from, fulfills, or cancels the source. The database prevents
 * duplicate source-target-type combinations.
 */
@Entity
@Table(
    name = "document_relations",
    uniqueConstraints =
        @UniqueConstraint(
            columnNames = {"source_document_id", "target_document_id", "relation_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRelation implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_document_id", nullable = false)
  private Document sourceDocument;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "target_document_id", nullable = false)
  private Document targetDocument;

  @Column(name = "relation_type", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private DocumentRelationType relationType;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
