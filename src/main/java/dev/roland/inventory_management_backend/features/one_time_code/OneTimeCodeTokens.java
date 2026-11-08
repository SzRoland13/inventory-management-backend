package dev.roland.inventory_management_backend.features.one_time_code;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.message.GenericMessageKey;
import lombok.experimental.UtilityClass;

/** Hashes bearer tokens before they are persisted. */
@UtilityClass
public class OneTimeCodeTokens {

  /**
   * Hashes a raw bearer token for deterministic database lookup.
   *
   * @param token raw token value
   * @return lowercase hexadecimal SHA-256 digest
   * @throws ApiException if SHA-256 is unavailable
   */
  public String hash(final String token) {
    try {
      final byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new ApiException(GenericMessageKey.GENERIC_ERROR, e);
    }
  }
}
