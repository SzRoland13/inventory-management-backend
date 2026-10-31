package dev.roland.inventory_management_backend.common.async;

/** Lifecycle of a durable asynchronous job. */
public enum OutboxStatus {
  PENDING,
  PUBLISHING,
  QUEUED,
  SENT,
  DLQ_PENDING,
  DLQ_PUBLISHING,
  FAILED
}
