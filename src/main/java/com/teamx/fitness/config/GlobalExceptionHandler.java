package com.teamx.fitness.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Global exception handler for formatting error responses.
 * Provides consistent JSON error format across the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** Prefix length to trim status code string representation. */
  private static final int STATUS_CODE_PREFIX_LENGTH = 4;

  /**
   * Handles ResponseStatusException and formats it as JSON.
   *
   * @param ex the exception
   * @return formatted error response
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleResponseStatusException(
      ResponseStatusException ex) {
    Map<String, Object> errorResponse = new HashMap<>();

    // Format error as "Forbidden" instead of "FORBIDDEN"
    String errorName = ex.getStatusCode().toString().substring(STATUS_CODE_PREFIX_LENGTH);
    errorName = errorName.charAt(0) + errorName.substring(1).toLowerCase();

    errorResponse.put("status", ex.getStatusCode().value());
    errorResponse.put("error", errorName);
    errorResponse.put("message", ex.getReason());

    return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
  }
}
