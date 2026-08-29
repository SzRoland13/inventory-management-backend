package dev.roland.inventory_management_backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Supported document types and their default numbering prefixes. */
@Getter
@AllArgsConstructor
public enum DocumentType {
  /** Represents the rfq value. */
  RFQ("RFQ"),
  /** Represents the supplier quotation value. */
  SUPPLIER_QUOTATION("SUP"),
  /** Represents the purchase order value. */
  PURCHASE_ORDER("PO"),
  /** Represents the goods receipt value. */
  GOODS_RECEIPT("GR"),
  /** Represents the customer rfq value. */
  CUSTOMER_RFQ("CRFQ"),
  /** Represents the sales quotation value. */
  SALES_QUOTATION("SQ"),
  /** Represents the sales order value. */
  SALES_ORDER("SO"),
  /** Represents the delivery note value. */
  DELIVERY_NOTE("DN"),
  /** Represents the invoice value. */
  INVOICE("INV"),
  /** Represents the transfer request value. */
  TRANSFER_REQUEST("TRQ"),
  /** Represents the transfer delivery value. */
  TRANSFER_DELIVERY("TD"),
  /** Represents the transfer receipt value. */
  TRANSFER_RECEIPT("TR"),
  /** Represents the stock adjustment value. */
  STOCK_ADJUSTMENT("SA"),
  /** Represents the return value. */
  RETURN("RE"),
  ;

  private final String defaultPrefix;
}
