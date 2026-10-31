package dev.roland.inventory_management_backend.common.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Message keys for asynchronous job and outbox failures. */
@Getter
@RequiredArgsConstructor
public enum AsyncJobMessageKey implements MessageKey {
  OUTBOX_ENCRYPTION_CONFIGURATION_INVALID("async.outbox.encryption-configuration-invalid"),
  OUTBOX_ENCRYPTION_FAILED("async.outbox.encryption-failed"),
  OUTBOX_PAYLOAD_INVALID("async.outbox.payload-invalid"),
  OUTBOX_PAYLOAD_SERIALIZATION_FAILED("async.outbox.payload-serialization-failed"),
  OUTBOX_JOB_NOT_FOUND("async.outbox.job-not-found"),
  OUTBOX_JOB_TYPE_UNSUPPORTED("async.outbox.job-type-unsupported"),
  RABBITMQ_PUBLISH_REJECTED("async.rabbitmq.publish-rejected"),
  RABBITMQ_MESSAGE_UNROUTABLE("async.rabbitmq.message-unroutable"),
  RABBITMQ_RETRY_PUBLISH_FAILED("async.rabbitmq.retry-publish-failed"),
  EMAIL_DELIVERY_FAILED("async.email.delivery-failed");

  private final String key;
}
