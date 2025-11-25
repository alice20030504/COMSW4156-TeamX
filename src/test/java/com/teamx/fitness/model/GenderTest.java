package com.teamx.fitness.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/** Covers parsing behaviour for {@link Gender}. */
@DisplayName("Gender enum parsing")
class GenderTest {

  @Test
  @DisplayName("fromValue accepts mixed case strings")
  void parsesMixedCase() {
    assertEquals(Gender.MALE, Gender.fromValue("male"));
    assertEquals(Gender.FEMALE, Gender.fromValue(" FeMale "));
  }

  @Test
  @DisplayName("fromValue rejects null, blank, and unsupported values")
  void rejectsInvalidValues() {
    assertThrows(ResponseStatusException.class, () -> Gender.fromValue(null));
    assertThrows(ResponseStatusException.class, () -> Gender.fromValue("  "));
    assertThrows(ResponseStatusException.class, () -> Gender.fromValue("other"));
  }
}
