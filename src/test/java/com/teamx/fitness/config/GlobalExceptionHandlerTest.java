package com.teamx.fitness.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.Method;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exercises {@link GlobalExceptionHandler}.
 */
@DisplayName("GlobalExceptionHandler formatting")
class GlobalExceptionHandlerTest {

  /** HTTP 200 status code. */
  private static final int STATUS_OK = 200;
  /** HTTP 400 status code. */
  private static final int STATUS_BAD_REQUEST = 400;
  /** HTTP 403 status code. */
  private static final int STATUS_FORBIDDEN = 403;

  /** Handler instance under test. */
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
  /** Cached reflection reference for creating MethodParameter objects. */
  private static final Method SAMPLE_METHOD;

  static {
    try {
      SAMPLE_METHOD =
          GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleMethod", String.class);
    } catch (NoSuchMethodException ex) {
      throw new IllegalStateException("Test setup failed", ex);
    }
  }

  @Test
  @DisplayName("Formats ResponseStatusException payload")
  void handlesResponseStatusException() {
    ResponseStatusException ex =
        new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");

    ResponseEntity<Map<String, Object>> response = handler.handleResponseStatusException(ex);

    assertEquals(STATUS_FORBIDDEN, response.getStatusCode().value());
    assertEquals("Forbidden", response.getBody().get("error"));
    assertEquals("Access denied", response.getBody().get("message"));
  }

  @Test
  @DisplayName("Formats bean validation errors")
  void handlesMethodArgumentNotValid() {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new PersonBean(), "person");
    bindingResult.rejectValue("field", "error", "Field must not be blank");
    MethodParameter parameter = new MethodParameter(SAMPLE_METHOD, 0);
    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(parameter, bindingResult);

    ResponseEntity<Map<String, Object>> response =
        handler.handleMethodArgumentNotValid(ex);

    assertEquals(STATUS_BAD_REQUEST, response.getStatusCode().value());
    assertTrue(((String) response.getBody().get("message")).contains("Field must not be blank"));
  }

  @Test
  @DisplayName("Formats constraint violations")
  void handlesConstraintViolations() {
    ConstraintViolationException ex = new ConstraintViolationException("Invalid parameter", null);

    ResponseEntity<Map<String, Object>> response =
        handler.handleConstraintViolation(ex);

    assertEquals(STATUS_BAD_REQUEST, response.getStatusCode().value());
    assertEquals("Invalid parameter", response.getBody().get("message"));
  }

  @Test
  @DisplayName("Formats date parsing errors")
  void handlesDateParse() {
    DateTimeParseException ex =
        new DateTimeParseException("Invalid format", "2024-13-40", 0);

    ResponseEntity<Map<String, Object>> response =
        handler.handleDateTimeParse(ex);

    assertEquals(STATUS_BAD_REQUEST, response.getStatusCode().value());
    assertEquals("Invalid date format. Use YYYY-MM-DD", response.getBody().get("message"));
  }

  @SuppressWarnings("unused")
  private void sampleMethod(String field) {
    // helper for MethodParameter
  }

  /** Simple bean with a readable property to satisfy binding result requirements. */
  private static final class PersonBean {
    /** Dummy field so BeanPropertyBindingResult has something to bind. */
    private String field;

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }
  }
}
