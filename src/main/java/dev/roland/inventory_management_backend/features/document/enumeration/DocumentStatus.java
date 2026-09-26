package dev.roland.inventory_management_backend.features.document.enumeration;

/** Defines the lifecycle states used when posting or managing documents. */
public enum DocumentStatus {
  /** Represents the draft value. */
  DRAFT,
  /** Represents the sent value. */
  SENT,
  /** Represents the confirmed value. */
  CONFIRMED,
  /** Represents the partially completed value. */
  PARTIALLY_COMPLETED,
  /** Represents the completed value. */
  COMPLETED,
  /** Represents the cancelled value. */
  CANCELLED
}
