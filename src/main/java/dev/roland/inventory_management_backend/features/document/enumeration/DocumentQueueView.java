package dev.roland.inventory_management_backend.features.document.enumeration;

/** Queue views supported by the document list endpoint. */
public enum DocumentQueueView {
  MY_DRAFTS,
  READY_TO_RECEIVE,
  READY_TO_SHIP,
  PARTIALLY_FULFILLED,
  ALL,
  TRANSFERS,
  IN_TRANSIT,
  RETURNS,
  STOCK_COUNTS,
  CORRECTIONS
}
