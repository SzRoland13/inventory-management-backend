package dev.roland.inventory_management_backend.common.dto;

import java.time.Instant;
import java.util.Map;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Wraps API results with a status, localization key, payload, parameters, and timestamp.
 *
 * @param <T> type of the response payload
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
  private boolean success;
  private String messageKey;
  private T payload;
  private Map<String, Object> params;
  private Instant timestamp;

  /**
   * Creates a successful API response with the supplied payload.
   *
   * @param <T> payload type
   * @param key localization key for the response
   * @param payload successful response data
   * @return successful API response
   */
  public static <T> ApiResponse<T> success(MessageKey key, T payload) {
    return new ApiResponse<>(true, key.getKey(), payload, null, Instant.now());
  }

  /**
   * Creates a failed API response without a payload.
   *
   * @param <T> payload type
   * @param key localization key for the failure
   * @return failed API response
   */
  public static <T> ApiResponse<T> failure(MessageKey key) {
    return new ApiResponse<>(false, key.getKey(), null, null, Instant.now());
  }

  /**
   * Creates a failed API response with localization parameters.
   *
   * @param <T> payload type
   * @param key localization key for the failure
   * @param params values used to interpolate the message
   * @return failed API response
   */
  public static <T> ApiResponse<T> failure(MessageKey key, Map<String, Object> params) {
    return new ApiResponse<>(false, key.getKey(), null, params, Instant.now());
  }

  /**
   * Creates a failed API response with a payload.
   *
   * @param <T> method type parameter
   * @param key key supplied to this method
   * @param payload payload supplied to this method
   * @return failure result
   */
  public static <T> ApiResponse<T> failure(MessageKey key, T payload) {
    return new ApiResponse<>(false, key.getKey(), payload, null, Instant.now());
  }
}
