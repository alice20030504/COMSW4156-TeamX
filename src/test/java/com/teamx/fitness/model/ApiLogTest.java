package com.teamx.fitness.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Exercises the JPA helper logic in {@link ApiLog}. */
@DisplayName("ApiLog entity")
class ApiLogTest {

  @Test
  @DisplayName("@PrePersist populates timestamp when missing")
  void onCreatePopulatesTimestamp() {
    ApiLog log = new ApiLog();
    log.onCreate();

    assertNotNull(log.getTimestamp(), "timestamp should be auto-populated");
  }

  @Test
  @DisplayName("@PrePersist preserves provided timestamp")
  void onCreatePreservesTimestamp() {
    ApiLog log = new ApiLog();
    LocalDateTime fixed = LocalDateTime.of(2024, 1, 1, 12, 0);
    log.setTimestamp(fixed);

    log.onCreate();

    assertEquals(fixed, log.getTimestamp());
  }
}
