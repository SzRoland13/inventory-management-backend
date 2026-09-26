package dev.roland.inventory_management_backend.common.exception.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.common.message.GenericMessageKey;
import dev.roland.inventory_management_backend.features.auth.message.AuthMessageKey;
import dev.roland.inventory_management_backend.features.user.message.UserMessageKey;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void mapsDomainExceptionsToTheirHttpStatusesAndMessageKeys() {
    final var apiFailure =
        handler.handleApiException(new ApiException(AuthMessageKey.INVALID_TOKEN));
    final var notFound =
        handler.handleNotFoundExceptions(new NotFoundException(UserMessageKey.USER_NOT_FOUND));
    final var unauthorized =
        handler.handleUnauthorized(new UnauthorizedException(AuthMessageKey.INVALID_TOKEN));

    assertEquals(HttpStatus.BAD_REQUEST, apiFailure.getStatusCode());
    assertEquals(AuthMessageKey.INVALID_TOKEN.getKey(), apiFailure.getBody().getMessageKey());
    assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
    assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.getStatusCode());
  }

  @Test
  void mapsUnexpectedAndValidationErrorsToConsistentResponses() {
    final var generic = handler.handleGenericException(new IllegalStateException("failure"));
    final BeanPropertyBindingResult binding =
        new BeanPropertyBindingResult(new Object(), "request");
    binding.addError(new FieldError("request", "email", "must be valid"));
    final var validation =
        handler.handleValidationErrors(new MethodArgumentNotValidException(null, binding));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generic.getStatusCode());
    assertEquals(GenericMessageKey.GENERIC_ERROR.getKey(), generic.getBody().getMessageKey());
    assertEquals(HttpStatus.BAD_REQUEST, validation.getStatusCode());
    assertEquals("must be valid", validation.getBody().getParams().get("email"));
  }
}
