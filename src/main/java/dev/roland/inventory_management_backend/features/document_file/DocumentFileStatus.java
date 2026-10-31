package dev.roland.inventory_management_backend.features.document_file;

/** Lifecycle state for a generated document file. */
public enum DocumentFileStatus {
  QUEUED,
  PROCESSING,
  COMPLETED,
  FAILED
}
