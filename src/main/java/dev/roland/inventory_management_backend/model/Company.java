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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.model.interfaces.IdInterface;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Company implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "email")
  private String email;

  @Column(name = "phone", length = 100)
  private String phone;

  @Column(name = "address", length = 500)
  private String address;

  @Column(name = "website")
  private String website;

  @Column(name = "tax_number", length = 100)
  private String taxNumber;

  @Column(name = "vat_number", length = 100)
  private String vatNumber;

  @Column(name = "registration_number", length = 100)
  private String registrationNumber;

  @Column(name = "bank_account")
  private String bankAccount;

  @Column(name = "iban", length = 100)
  private String iban;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "preferred_currency_id")
  private Currency preferredCurrency;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
