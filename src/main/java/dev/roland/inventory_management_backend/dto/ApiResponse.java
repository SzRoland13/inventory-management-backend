package dev.roland.inventory_management_backend.dto;

import dev.roland.inventory_management_backend.messageKey.MessageKey;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String messageKey;
    private T payload;

    public static <T> ApiResponse<T> success(MessageKey key, T payload) {
        return new ApiResponse<>(true, key.getKey(), payload);
    }

    public static <T> ApiResponse<T> failure(MessageKey key) {
        return new ApiResponse<>(false, key.getKey(), null);
    }

    public static <T> ApiResponse<T> failure(MessageKey key, T payload) {
        return new ApiResponse<>(false, key.getKey(), payload);
    }

}
