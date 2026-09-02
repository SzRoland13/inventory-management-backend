package dev.roland.inventory_management_backend.features.product_attribute_definition;

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
import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.common.persistance.IdInterface;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;
import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;
import dev.roland.inventory_management_backend.features.product_attribute_value.enumeration.ProductAttributeValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Defines a configurable attribute that can be assigned to products.
 *
 * <p>An attribute definition may belong to a {@link Company} and declares the value type, such as
 * text, number, date, boolean, or fixed option. Its values are stored in {@link
 * ProductAttributeValue}, and fixed choices are stored in {@link ProductAttributeOption}.
 */
@Entity
@Table(
    name = "product_attribute_definitions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttributeDefinition implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @Column(name = "code", nullable = false, length = 100)
  private String code;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "value_type", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private ProductAttributeValueType valueType;

  @Column(name = "is_required", nullable = false)
  private boolean required;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
