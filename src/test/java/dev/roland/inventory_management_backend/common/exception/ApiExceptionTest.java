package dev.roland.inventory_management_backend.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;

class ApiExceptionTest {
  @Test
  void supportsMissingAndPresentInterpolationParameters() {
    assertNull(new ApiException(NotFoundMessageKey.USER).getParams());
    assertEquals(
        Map.of("id", 3L), new ApiException(NotFoundMessageKey.USER, Map.of("id", 3L)).getParams());
  }
}
