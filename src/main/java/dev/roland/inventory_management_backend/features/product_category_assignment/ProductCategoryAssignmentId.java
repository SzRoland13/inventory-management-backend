package dev.roland.inventory_management_backend.features.product_category_assignment;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Identifies a product-category assignment by its composite key. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductCategoryAssignmentId implements Serializable {

  @Column(name = "product_id")
  private Long productId;

  @Column(name = "category_id")
  private Long categoryId;
}
