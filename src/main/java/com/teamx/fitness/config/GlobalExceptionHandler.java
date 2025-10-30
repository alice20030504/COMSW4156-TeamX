package com.teamx.fitness.config;

import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
   * @param ex the ResponseStatusException raised by the application
   * @return formatted error response with the same HTTP status as the exception
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

  /**
   * Handles bean validation errors for @Valid request bodies.
   *
   * @param ex validation exception produced by Spring when a @Valid body fails
   * @return 400 Bad Request with a concise validation message
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getBindingResult().getFieldErrors().stream()
        .map(f -> f.getField() + ": " + f.getDefaultMessage())
        .findFirst().orElse("Validation failed"));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  /**
   * Handles validation errors raised from constraints on parameters.
   *
   * @param ex constraint violation exception for parameter-level validation
   * @return 400 Bad Request with details about the violated constraint
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolation(
      ConstraintViolationException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  /**
   * Handles invalid date parsing errors like LocalDate.parse.
   *
   * @param ex parsing exception thrown when a date string has an invalid format
   * @return 400 Bad Request with a human-readable hint for the expected format
   */
  @ExceptionHandler(DateTimeParseException.class)
  public ResponseEntity<Map<String, Object>> handleDateTimeParse(DateTimeParseException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", "Invalid date format. Use YYYY-MM-DD");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }
}
