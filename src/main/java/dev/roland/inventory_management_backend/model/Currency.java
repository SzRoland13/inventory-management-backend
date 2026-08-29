package dev.roland.inventory_management_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import dev.roland.inventory_management_backend.model.interfaces.IdInterface;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a supported currency used for company preferences, products, and documents.
 *
 * <p>The {@code currencies} table is referenced by {@link Company}, {@link Product}, {@link
 * Document}, and {@link DocumentLine}. Currency rows are shared reference data rather than
 * company-owned records.
 */
@Entity
@Table(name = "currencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Currency implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "code", nullable = false, unique = true, length = 10)
  private String code;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "symbol", length = 10)
  private String symbol;
}
