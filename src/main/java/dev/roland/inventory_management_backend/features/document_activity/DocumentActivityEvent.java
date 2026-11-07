package dev.roland.inventory_management_backend.features.document_activity;

/** Structured document lifecycle and file events recorded in the activity history. */
public enum DocumentActivityEvent {
  CREATED,
  EDITED,
  POSTED,
  CONFIRMED,
  REMAINDER_CLOSED,
  REMAINDER_REOPENED,
  FILE_UPLOADED,
  PDF_QUEUED,
  APPROVED,
  REJECTED,
  FINALIZED,
  SETTLEMENT_ASSIGNED
}
