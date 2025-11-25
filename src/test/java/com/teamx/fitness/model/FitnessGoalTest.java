package com.teamx.fitness.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/** Covers parsing for {@link FitnessGoal}. */
@DisplayName("FitnessGoal enum parsing")
class FitnessGoalTest {

  @Test
  @DisplayName("fromValue handles valid options")
  void parsesValidValues() {
    assertEquals(FitnessGoal.CUT, FitnessGoal.fromValue("cut"));
    assertEquals(FitnessGoal.BULK, FitnessGoal.fromValue(" BULK "));
  }

  @Test
  @DisplayName("fromValue rejects null, blank, and unsupported inputs")
  void rejectsInvalidValues() {
    assertThrows(ResponseStatusException.class, () -> FitnessGoal.fromValue(null));
    assertThrows(ResponseStatusException.class, () -> FitnessGoal.fromValue("  "));
    assertThrows(ResponseStatusException.class, () -> FitnessGoal.fromValue("maintain"));
  }
}
