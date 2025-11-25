package com.teamx.fitness.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests that exercise the key calculation methods exposed by {@link PersonService}.
 */
@DisplayName("PersonService core calculations")
class PersonServiceTest {

  private PersonService personService;

  @BeforeEach
  void setUp() {
    personService = new PersonService();
  }

  /**
   * Confirms BMI computations produce expected categories and guard against null input.
   */
  @ParameterizedTest
  @MethodSource("calculateBmiScenarios")
  @DisplayName("calculateBMI handles valid, boundary, and invalid inputs")
  void calculateBMIHandlesScenarios(
      String description, Double weight, Double height, Double expectedValue, boolean expectNull) {

    Double result = personService.calculateBMI(weight, height);

    if (expectNull) {
      assertNull(result, description);
    } else {
      assertNotNull(result, description);
      assertEquals(expectedValue, result, 0.05, description);
    }
  }

  private static Stream<Arguments> calculateBmiScenarios() {
    return Stream.of(
        Arguments.of("Valid: typical adult metrics", 70.0, 175.0, 22.86, false),
        Arguments.of("Boundary: underweight classification", 50.0, 180.0, 15.43, false),
        Arguments.of("Boundary: overweight classification", 85.0, 178.0, 26.82, false),
        Arguments.of("Boundary: obese classification", 110.0, 170.0, 38.06, false)
    );
  }

  @Test
  @DisplayName("calculateBMI throws for null inputs and invalid ranges")
  void calculateBmiInvalidInputs() {
    // null inputs
    org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> personService.calculateBMI(null, 170.0));
    org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> personService.calculateBMI(70.0, null));

    // non-positive
    org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> personService.calculateBMI(0.0, 170.0));
    org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> personService.calculateBMI(-5.0, 170.0));

    // unreasonably large
    org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> personService.calculateBMI(10000.0, 170.0));
    org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> personService.calculateBMI(70.0, 10000.0));
  }

  /**
   * Validates age calculations across normal, boundary, and null inputs.
   */
  @ParameterizedTest
  @MethodSource("calculateAgeScenarios")
  @DisplayName("calculateAge handles valid, boundary, and invalid inputs")
  void calculateAgeHandlesScenarios(
      String description, LocalDate birthDate, Integer explicitExpectedAge, boolean expectNull) {

    Integer result = personService.calculateAge(birthDate);

    if (expectNull) {
      assertNull(result, description);
      return;
    }

    assertNotNull(result, description);
    if (explicitExpectedAge != null) {
      assertEquals(explicitExpectedAge, result, description);
    } else {
      int expectedAge = Period.between(birthDate, LocalDate.now()).getYears();
      assertEquals(expectedAge, result, description);
    }
  }

  private static Stream<Arguments> calculateAgeScenarios() {
    return Stream.of(
        Arguments.of(
            "Valid: adult age calculation",
            LocalDate.now().minusYears(29).minusDays(10),
            null,
            false),
        Arguments.of("Boundary: born today -> age 0", LocalDate.now(), 0, false),
        Arguments.of("Invalid: null birth date", null, null, true));
  }

  /**
   * Exercises BMR computation for male/female cases while checking null guards.
   */
  @ParameterizedTest
  @MethodSource("calculateBmrScenarios")
  @DisplayName("calculateBMR handles valid, boundary, and invalid inputs")
  void calculateBMRHandlesScenarios(
      String description,
      Double weight,
      Double height,
      Integer age,
      boolean isMale,
      Double expectedValue,
      boolean expectNull) {

    Double result = personService.calculateBMR(weight, height, age, isMale);

    if (expectNull) {
      assertNull(result, description);
    } else {
      assertNotNull(result, description);
      assertEquals(expectedValue, result, 0.5, description);
    }
  }

  private static Stream<Arguments> calculateBmrScenarios() {
    return Stream.of(
        Arguments.of("Valid: male adult", 80.0, 180.0, 30, true, 1853.632, false),
        Arguments.of("Boundary: newborn female", 50.0, 160.0, 0, false, 1405.623, false),
        Arguments.of("Invalid: missing weight", null, 165.0, 35, true, null, true));
  }

  /**
   * Ensures calorie needs calculation reflects activity bracket logic and null-safety.
   */
  @ParameterizedTest
  @MethodSource("calculateDailyCalorieNeedsScenarios")
  @DisplayName("calculateDailyCalorieNeeds handles valid, boundary, and invalid inputs")
  void calculateDailyCalorieNeedsScenarios(
      String description,
      Double bmr,
      Integer weeklyTrainingFreq,
      Double expectedValue,
      boolean expectNull) {

    Double result = personService.calculateDailyCalorieNeeds(bmr, weeklyTrainingFreq);

    if (expectNull) {
      assertNull(result, description);
    } else {
      assertNotNull(result, description);
      assertEquals(expectedValue, result, 0.5, description);
    }
  }

  private static Stream<Arguments> calculateDailyCalorieNeedsScenarios() {
    return Stream.of(
        Arguments.of("Valid: moderate activity", 1600.0, 3, 2480.0, false),
        Arguments.of("Valid: light activity", 1600.0, 2, 2200.0, false),
        Arguments.of("Valid: very active", 1700.0, 5, 2932.5, false),
        Arguments.of("Boundary: sedentary baseline", 1800.0, 0, 2160.0, false),
        Arguments.of("Boundary: extra active baseline", 1700.0, 7, 3230.0, false),
        Arguments.of("Invalid: missing BMR", null, 4, null, true),
        Arguments.of("Invalid: missing frequency", 1500.0, null, null, true));
  }
}
