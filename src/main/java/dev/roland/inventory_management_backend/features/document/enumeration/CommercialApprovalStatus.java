package dev.roland.inventory_management_backend.features.document.enumeration;

/** Approval lifecycle for a supplier bill or customer invoice draft. */
public enum CommercialApprovalStatus {
  NOT_REQUIRED,
  PENDING,
  APPROVED,
  REJECTED
}
