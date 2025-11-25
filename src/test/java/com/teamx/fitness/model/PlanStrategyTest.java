package com.teamx.fitness.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/** Covers parsing for {@link PlanStrategy}. */
@DisplayName("PlanStrategy enum parsing")
class PlanStrategyTest {

  @Test
  @DisplayName("fromValue handles valid strategies")
  void parsesValidValues() {
    assertEquals(PlanStrategy.WORKOUT, PlanStrategy.fromValue("workout"));
    assertEquals(PlanStrategy.DIET, PlanStrategy.fromValue(" DIET "));
    assertEquals(PlanStrategy.BOTH, PlanStrategy.fromValue("BoTh"));
  }

  @Test
  @DisplayName("fromValue rejects null, blank, and unsupported strings")
  void rejectsInvalidValues() {
    assertThrows(ResponseStatusException.class, () -> PlanStrategy.fromValue(null));
    assertThrows(ResponseStatusException.class, () -> PlanStrategy.fromValue(" "));
    assertThrows(ResponseStatusException.class, () -> PlanStrategy.fromValue("custom"));
  }
}
