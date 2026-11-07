package dev.roland.inventory_management_backend.features.document.enumeration;

/** Stock disposition selected for a returned line. */
public enum ReturnDisposition {
  RESTOCK,
  QUARANTINE,
  DAMAGED,
  SUPPLIER_RETURN
}
