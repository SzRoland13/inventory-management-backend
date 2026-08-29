package dev.roland.inventory_management_backend.model;

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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.model.interfaces.IdInterface;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a hierarchical product category.
 *
 * <p>A category may belong to a {@link Company} and may reference another category as its parent.
 * Products are associated with categories through {@link ProductCategoryAssignment}, allowing a
 * product to belong to multiple categories.
 */
@Entity
@Table(
    name = "product_categories",
    uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private ProductCategory parent;

  @Column(name = "code", nullable = false, length = 100)
  private String code;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "description")
  private String description;

  @Builder.Default
  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
