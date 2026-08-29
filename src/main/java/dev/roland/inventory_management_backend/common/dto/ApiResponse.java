package dev.roland.inventory_management_backend.dto;

import java.time.Instant;
import java.util.Map;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
  private boolean success;
  private String messageKey;
  private T payload;
  private Map<String, Object> params;
  private Instant timestamp;

  public static <T> ApiResponse<T> success(MessageKey key, T payload) {
    return new ApiResponse<>(true, key.getKey(), payload, null, Instant.now());
  }

  public static <T> ApiResponse<T> failure(MessageKey key) {
    return new ApiResponse<>(false, key.getKey(), null, null, Instant.now());
  }

  public static <T> ApiResponse<T> failure(MessageKey key, Map<String, Object> params) {
    return new ApiResponse<>(false, key.getKey(), null, params, Instant.now());
  }

  public static <T> ApiResponse<T> failure(MessageKey key, T payload) {
    return new ApiResponse<>(false, key.getKey(), payload, null, Instant.now());
  }
}
