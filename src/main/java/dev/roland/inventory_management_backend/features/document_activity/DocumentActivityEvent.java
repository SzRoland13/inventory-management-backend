package dev.roland.inventory_management_backend.features.document_activity;

/** Structured document lifecycle and file events recorded in the activity history. */
public enum DocumentActivityEvent {
  CREATED,
  EDITED,
  POSTED,
  CONFIRMED,
  REMAINDER_CLOSED,
  FILE_UPLOADED,
  PDF_QUEUED
}
