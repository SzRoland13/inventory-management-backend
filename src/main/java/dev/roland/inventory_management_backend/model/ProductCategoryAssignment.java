package dev.roland.inventory_management_backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Join entity associating a {@link Product} with a {@link ProductCategory}.
 *
 * <p>The pair of foreign keys is represented by {@link ProductCategoryAssignmentId} and forms the
 * composite primary key. This model exists so the product-category relationship can carry its own
 * creation timestamp.
 */
@Entity
@Table(name = "product_category_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryAssignment {

  @EmbeddedId private ProductCategoryAssignmentId id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("productId")
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("categoryId")
  @JoinColumn(name = "category_id", nullable = false)
  private ProductCategory category;

  @CreationTimestamp private LocalDateTime createdAt;

  @PrePersist
  void ensureId() {
    if (id == null && product != null && category != null) {
      id = new ProductCategoryAssignmentId(product.getId(), category.getId());
    }
  }
}
