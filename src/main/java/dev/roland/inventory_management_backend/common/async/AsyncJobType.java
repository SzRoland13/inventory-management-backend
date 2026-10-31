package dev.roland.inventory_management_backend.common.async;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Stable job types and their RabbitMQ topology keys. */
@Getter
@RequiredArgsConstructor
public enum AsyncJobType {
  EMAIL(
      "inventory.email",
      "email",
      "inventory.email.retry",
      "email-retry",
      "inventory.email.dlq",
      "email-dlq"),
  DOCUMENT_PDF(
      "inventory.document-pdf",
      "document-pdf",
      "inventory.document-pdf.retry",
      "document-pdf-retry",
      "inventory.document-pdf.dlq",
      "document-pdf-dlq");

  private final String queueName;
  private final String routingKey;
  private final String retryQueueName;
  private final String retryRoutingKey;
  private final String deadLetterQueueName;
  private final String deadLetterRoutingKey;
}
