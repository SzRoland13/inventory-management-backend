package dev.roland.inventory_management_backend.exception.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.exception.ApiException;
import dev.roland.inventory_management_backend.exception.NotFoundException;
import dev.roland.inventory_management_backend.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.message_key.GenericMessageKey;

/** Converts domain and validation exceptions into consistent API responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** Handles the corresponding exception as a consistent API response. */
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.failure(ex.getMessageKey(), ex.getParams()));
  }

  /** Handles the corresponding exception as a consistent API response. */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFoundExceptions(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.failure(ex.getMessageKey()));
  }

  /** Handles the corresponding exception as a consistent API response. */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.failure(ex.getMessageKey(), ex.getParams()));
  }

  /** Handles the corresponding exception as a consistent API response. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.failure(GenericMessageKey.GENERIC_ERROR));
  }

  /** Handles the corresponding exception as a consistent API response. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<String>> handleValidationErrors(
      MethodArgumentNotValidException ex) {
    Map<String, Object> params = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> params.put(error.getField(), error.getDefaultMessage()));

    return ResponseEntity.badRequest()
        .body(ApiResponse.failure(GenericMessageKey.VALIDATION_ERROR, params));
  }
}
