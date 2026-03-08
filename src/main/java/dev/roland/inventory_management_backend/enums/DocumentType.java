package dev.roland.inventory_management_backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentType {
  RFQ("RFQ"),
  SUPPLIER_QUOTATION("SUP"),
  PURCHASE_ORDER("PO"),
  GOODS_RECEIPT("GR"),
  CUSTOMER_RFQ("CRFQ"),
  SALES_QUOTATION("SQ"),
  SALES_ORDER("SO"),
  DELIVERY_NOTE("DN"),
  INVOICE("INV"),
  TRANSFER_REQUEST("TRQ"),
  TRANSFER_DELIVERY("TD"),
  TRANSFER_RECEIPT("TR"),
  STOCK_ADJUSTMENT("SA"),
  RETURN("RE"),
  ;

  private final String defaultPrefix;
}
