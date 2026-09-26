package dev.roland.inventory_management_backend.common.exception.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.common.message.GenericMessageKey;

/** Converts domain and validation exceptions into consistent API responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Converts a domain API failure into a bad-request response.
   *
   * @param ex exception carrying the response message and parameters
   * @return bad-request response for the client
   */
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Void>> handleApiException(final ApiException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.failure(ex.getMessageKey(), ex.getParams()));
  }

  /**
   * Converts a missing-resource failure into a not-found response.
   *
   * @param ex exception carrying the missing-resource message
   * @return not-found response for the client
   */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFoundExceptions(final NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.failure(ex.getMessageKey()));
  }

  /**
   * Converts an authorization failure into an unauthorized response.
   *
   * @param ex exception carrying the authorization message
   * @return unauthorized response for the client
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnauthorized(final UnauthorizedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.failure(ex.getMessageKey(), ex.getParams()));
  }

  /**
   * Converts an unexpected failure into a generic server-error response.
   *
   * @param ex unexpected exception raised while handling the request
   * @return generic server-error response for the client
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(final Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.failure(GenericMessageKey.GENERIC_ERROR));
  }

  /**
   * Converts request validation failures into a response containing field errors.
   *
   * @param ex exception containing the rejected request fields
   * @return bad-request response with field-specific validation messages
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<String>> handleValidationErrors(
      final MethodArgumentNotValidException ex) {
    return validationResponse(ex);
  }

  /** Converts model-attribute validation failures into field-level message keys. */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<String>> handleBindingErrors(final BindException ex) {
    return validationResponse(ex);
  }

  private ResponseEntity<ApiResponse<String>> validationResponse(final BindException ex) {
    final Map<String, Object> params = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            error ->
                params.put(
                    error.getField(),
                    error.isBindingFailure()
                        ? GenericMessageKey.INVALID_REQUEST_FORMAT.getKey()
                        : error.getDefaultMessage()));

    return ResponseEntity.badRequest()
        .body(ApiResponse.failure(GenericMessageKey.VALIDATION_ERROR, params));
  }
}
