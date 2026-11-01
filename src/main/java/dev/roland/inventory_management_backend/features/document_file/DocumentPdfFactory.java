package dev.roland.inventory_management_backend.features.document_file;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.utils.DocumentTotalsCalculator;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import lombok.RequiredArgsConstructor;

/** Builds localized HTML snapshots for document PDFs. */
@Component
@RequiredArgsConstructor
public class DocumentPdfFactory {
  private static final String TEMPLATE = "documents/document";

  private final ITemplateEngine templateEngine;
  private final MessageSource documentMessages;

  /**
   * Creates the HTML representation of a document.
   *
   * @param document document value
   * @param languageTag language tag value
   * @return the requested result
   */
  public String createHtml(final Document document, final String languageTag) {
    final Locale locale = Locale.forLanguageTag(languageTag == null ? "en" : languageTag);
    final Context context = new Context(locale);
    final List<Map<String, Object>> lines =
        document.getLines() == null
            ? List.of()
            : document.getLines().stream().map(line -> lineData(line, locale)).toList();
    final DocumentTotalsCalculator.Totals totals =
        DocumentTotalsCalculator.calculate(
            document.getLines() == null ? List.of() : document.getLines());
    context.setVariables(
        Map.ofEntries(
            Map.entry(
                "documentType", message("pdf.document.type." + document.getType().name(), locale)),
            Map.entry(
                "documentStatus",
                message("pdf.document.status." + document.getStatus().name(), locale)),
            Map.entry(
                "number",
                document.getDocumentNumber() == null
                    ? message("pdf.document.draft", locale)
                    : document.getDocumentNumber()),
            Map.entry(
                "date",
                document
                    .getDocumentDate()
                    .format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))),
            Map.entry("numberLabel", message("pdf.label.number", locale)),
            Map.entry("dateLabel", message("pdf.label.date", locale)),
            Map.entry("partnerLabel", message("pdf.label.partner", locale)),
            Map.entry("warehouseLabel", message("pdf.label.warehouse", locale)),
            Map.entry("itemsTitle", message("pdf.label.items", locale)),
            Map.entry("quantityHint", message("pdf.label.quantity_hint", locale)),
            Map.entry("productLabel", message("pdf.label.product", locale)),
            Map.entry("codeLabel", message("pdf.label.code", locale)),
            Map.entry("unitLabel", message("pdf.label.unit", locale)),
            Map.entry("quantityLabel", message("pdf.label.quantity", locale)),
            Map.entry("unitPriceLabel", message("pdf.label.unit_price", locale)),
            Map.entry("vatRateLabel", message("pdf.label.vat_rate", locale)),
            Map.entry("netLabel", message("pdf.label.net", locale)),
            Map.entry("vatLabel", message("pdf.label.vat", locale)),
            Map.entry("grossLabel", message("pdf.label.gross", locale)),
            Map.entry("totalLabel", message("pdf.label.total", locale)),
            Map.entry("notesLabel", message("pdf.label.notes", locale)),
            Map.entry("partnerName", value(document.getPartnerNameSnapshot())),
            Map.entry("partnerAddress", value(document.getPartnerAddressSnapshot())),
            Map.entry(
                "warehouseName",
                document.getSourceWarehouseNameSnapshot() != null
                    ? document.getSourceWarehouseNameSnapshot()
                    : value(document.getTargetWarehouseNameSnapshot())),
            Map.entry("notes", value(document.getNotes())),
            Map.entry(
                "currencyCode",
                document.getCurrency() == null ? "" : document.getCurrency().getCode()),
            Map.entry("lines", lines),
            Map.entry("netTotal", formatMoney(totals.net(), locale)),
            Map.entry("taxTotal", formatMoney(totals.tax(), locale)),
            Map.entry("grossTotal", formatMoney(totals.gross(), locale))));
    return templateEngine.process(TEMPLATE, context);
  }

  private Map<String, Object> lineData(final DocumentLine line, final Locale locale) {
    final var quantity = line.getQuantity() == null ? BigDecimal.ZERO : line.getQuantity();
    final var price = line.getUnitPrice() == null ? BigDecimal.ZERO : line.getUnitPrice();
    final var amounts = DocumentTotalsCalculator.line(line);
    final var vatRate =
        line.getVatRateSnapshot() == null ? BigDecimal.ZERO : line.getVatRateSnapshot();
    return Map.ofEntries(
        Map.entry("product", value(line.getProductNameSnapshot())),
        Map.entry("code", value(line.getProductSkuSnapshot())),
        Map.entry("unit", value(line.getUnitNameSnapshot())),
        Map.entry("quantity", formatQuantity(quantity, locale)),
        Map.entry("price", formatMoney(price, locale)),
        Map.entry("vatRate", formatQuantity(vatRate, locale)),
        Map.entry("net", formatMoney(amounts.net(), locale)),
        Map.entry("tax", formatMoney(amounts.tax(), locale)),
        Map.entry("gross", formatMoney(amounts.gross(), locale)));
  }

  private String message(final String key, final Locale locale) {
    return documentMessages.getMessage(key, null, locale);
  }

  private String formatMoney(final BigDecimal value, final Locale locale) {
    final NumberFormat formatter = NumberFormat.getNumberInstance(locale);
    formatter.setMinimumFractionDigits(2);
    formatter.setMaximumFractionDigits(2);
    return formatter.format(value);
  }

  private String formatQuantity(final BigDecimal value, final Locale locale) {
    final NumberFormat formatter = NumberFormat.getNumberInstance(locale);
    formatter.setMaximumFractionDigits(8);
    return formatter.format(value);
  }

  private String value(final String value) {
    return value == null ? "" : value;
  }
}
