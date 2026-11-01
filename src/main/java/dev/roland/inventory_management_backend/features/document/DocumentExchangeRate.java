package dev.roland.inventory_management_backend.features.document;

import java.math.BigDecimal;

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

import dev.roland.inventory_management_backend.features.currency.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Immutable-by-workflow exchange-rate snapshot belonging to a document. */
@Entity
@Table(
    name = "document_exchange_rates",
    uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "source_currency_id"}))
@Getter
@Setter
@NoArgsConstructor
public class DocumentExchangeRate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "document_id", nullable = false)
  private Document document;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_currency_id", nullable = false)
  private Currency sourceCurrency;

  @Column(nullable = false, precision = 24, scale = 12)
  private BigDecimal rate;
}
