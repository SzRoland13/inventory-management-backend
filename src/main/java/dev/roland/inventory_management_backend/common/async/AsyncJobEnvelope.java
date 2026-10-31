package dev.roland.inventory_management_backend.common.async;

import java.util.UUID;

/**
 * Broker envelope identifying a durable job and preserving its protected context.
 *
 * @param id durable job identifier
 * @param jobType kind of asynchronous job
 * @param encryptedPayload protected job payload
 * @param companyId associated company, when present
 * @param documentId associated document, when present
 */
public record AsyncJobEnvelope(
    UUID id, AsyncJobType jobType, byte[] encryptedPayload, Long companyId, Long documentId) {

  /**
   * Copies the payload to keep the envelope immutable.
   *
   * @param id durable job identifier
   * @param jobType kind of asynchronous job
   * @param encryptedPayload protected job payload
   * @param companyId associated company, when present
   * @param documentId associated document, when present
   */
  public AsyncJobEnvelope {
    encryptedPayload = encryptedPayload.clone();
  }

  @Override
  public byte[] encryptedPayload() {
    return encryptedPayload.clone();
  }
}
