package dev.roland.inventory_management_backend.features.document.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import lombok.experimental.UtilityClass;

/** Applies the Release 1 monetary rule: unit prices are net and each line is rounded to cents. */
@UtilityClass
public final class DocumentTotalsCalculator {
  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

  /**
   * Calculates monetary amounts for a document line.
   *
   * @param line line value
   * @return the requested result
   */
  public static LineAmounts line(final DocumentLine line) {
    final BigDecimal quantity = line.getQuantity() == null ? BigDecimal.ZERO : line.getQuantity();
    final BigDecimal price = line.getUnitPrice() == null ? BigDecimal.ZERO : line.getUnitPrice();
    final BigDecimal net = price.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    final BigDecimal rate =
        line.getVatRateSnapshot() == null ? BigDecimal.ZERO : line.getVatRateSnapshot();
    final BigDecimal tax = net.multiply(rate).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    return new LineAmounts(net, tax, net.add(tax));
  }

  /**
   * Calculates totals for the supplied document lines.
   *
   * @param lines lines value
   * @return the requested result
   */
  public static Totals calculate(final Iterable<DocumentLine> lines) {
    BigDecimal net = BigDecimal.ZERO;
    BigDecimal tax = BigDecimal.ZERO;
    BigDecimal gross = BigDecimal.ZERO;
    for (DocumentLine line : lines) {
      final LineAmounts amounts = line(line);
      net = net.add(amounts.net());
      tax = tax.add(amounts.tax());
      gross = gross.add(amounts.gross());
    }
    return new Totals(
        net.setScale(2, RoundingMode.HALF_UP),
        tax.setScale(2, RoundingMode.HALF_UP),
        gross.setScale(2, RoundingMode.HALF_UP));
  }

  /**
   * Defines the line amounts document API contract.
   *
   * @param gross gross value
   * @param net net value
   * @param tax tax value
   */
  public record LineAmounts(BigDecimal net, BigDecimal tax, BigDecimal gross) {}

  /**
   * Defines the totals document API contract.
   *
   * @param gross gross value
   * @param net net value
   * @param tax tax value
   */
  public record Totals(BigDecimal net, BigDecimal tax, BigDecimal gross) {}
}
