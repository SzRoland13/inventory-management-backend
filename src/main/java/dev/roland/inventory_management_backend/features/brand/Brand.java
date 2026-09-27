package dev.roland.inventory_management_backend.features.brand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import dev.roland.inventory_management_backend.common.persistance.IdInterface;
import dev.roland.inventory_management_backend.features.company.Company;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A brand owned by a company and shared by its products. */
@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
public class Brand implements IdInterface<Long> {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;
}
