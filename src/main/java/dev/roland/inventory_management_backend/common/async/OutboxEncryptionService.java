package dev.roland.inventory_management_backend.common.async;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.AsyncJobMessageKey;

/** Encrypts persisted job payloads with AES-GCM. */
@Service
public class OutboxEncryptionService {

  private static final int NONCE_BYTES = 12;
  private static final int TAG_BITS = 128;
  private final byte[] secret;
  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Creates an encryptor using the configured secret.
   *
   * @param secret encryption secret
   */
  public OutboxEncryptionService(@Value("${app.outbox.encryption-secret:}") final String secret) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Encrypts payload bytes with authenticated encryption.
   *
   * @param plaintext bytes to encrypt
   * @return nonce followed by authenticated ciphertext
   * @throws AsyncJobException if encryption configuration or operation is invalid
   */
  public byte[] encrypt(final byte[] plaintext) {
    if (secret.length < 32) {
      throw new AsyncJobException(AsyncJobMessageKey.OUTBOX_ENCRYPTION_CONFIGURATION_INVALID, true);
    }
    final byte[] nonce = new byte[NONCE_BYTES];
    secureRandom.nextBytes(nonce);
    try {
      final byte[] cipherText = cipher(Cipher.ENCRYPT_MODE, nonce).doFinal(plaintext);
      final byte[] result = Arrays.copyOf(nonce, nonce.length + cipherText.length);
      System.arraycopy(cipherText, 0, result, nonce.length, cipherText.length);
      return result;
    } catch (GeneralSecurityException e) {
      throw new AsyncJobException(AsyncJobMessageKey.OUTBOX_ENCRYPTION_FAILED, true, e);
    }
  }

  /**
   * Decrypts an authenticated payload.
   *
   * @param encrypted nonce followed by authenticated ciphertext
   * @return decrypted payload bytes
   * @throws AsyncJobException if the payload is invalid or decryption fails
   */
  public byte[] decrypt(final byte[] encrypted) {
    if (secret.length < 32 || encrypted.length <= NONCE_BYTES) {
      throw new AsyncJobException(AsyncJobMessageKey.OUTBOX_PAYLOAD_INVALID, true);
    }
    final byte[] nonce = Arrays.copyOf(encrypted, NONCE_BYTES);
    try {
      return cipher(Cipher.DECRYPT_MODE, nonce)
          .doFinal(Arrays.copyOfRange(encrypted, NONCE_BYTES, encrypted.length));
    } catch (GeneralSecurityException e) {
      throw new AsyncJobException(AsyncJobMessageKey.OUTBOX_PAYLOAD_INVALID, true, e);
    }
  }

  private Cipher cipher(final int mode, final byte[] nonce) throws GeneralSecurityException {
    final byte[] key = MessageDigest.getInstance("SHA-256").digest(secret);
    final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(mode, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, nonce));
    return cipher;
  }
}
