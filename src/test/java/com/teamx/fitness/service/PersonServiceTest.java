package com.teamx.fitness.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests that exercise the key calculation methods exposed by {@link PersonService}.
 */
@DisplayName("PersonService core calculations")
class PersonServiceTest {

  /** Allowable delta for BMI assertions. */
  private static final double BMI_ASSERT_TOLERANCE = 0.05;
  /** Allowable delta for calorie assertions. */
  private static final double CALORIE_ASSERT_TOLERANCE = 0.5;
  /** Typical adult weight used in scenarios (kg). */
  private static final double WEIGHT_TYPICAL_KG = 70.0;
  /** Underweight adult weight (kg). */
  private static final double WEIGHT_UNDER_KG = 50.0;
  /** Overweight adult weight (kg). */
  private static final double WEIGHT_OVER_KG = 85.0;
  /** Obese adult weight (kg). */
  private static final double WEIGHT_OBESE_KG = 110.0;
  /** Female newborn weight for BMR calculation (kg). */
  private static final double WEIGHT_NEWBORN_FEMALE_KG = 50.0;
  /** Male adult weight for BMR calculation (kg). */
  private static final double WEIGHT_MALE_KG = 80.0;
  /** Weight used when testing invalid BMI input (kg). */
  private static final double WEIGHT_INVALID_KG = 0.0;
  /** Negative weight used for validation (kg). */
  private static final double WEIGHT_NEGATIVE_KG = -5.0;
  /** Extremely large weight (kg). */
  private static final double WEIGHT_EXTREME_KG = 10000.0;
  /** Typical adult height used in scenarios (cm). */
  private static final double HEIGHT_TYPICAL_CM = 175.0;
  /** Underweight sample height (cm). */
  private static final double HEIGHT_TALL_CM = 180.0;
  /** Overweight sample height (cm). */
  private static final double HEIGHT_OVER_CM = 178.0;
  /** Obese sample height (cm). */
  private static final double HEIGHT_SHORT_CM = 170.0;
  /** Female newborn height for BMR calculation (cm). */
  private static final double HEIGHT_NEWBORN_FEMALE_CM = 160.0;
  /** Male adult height for BMR calculation (cm). */
  private static final double HEIGHT_MALE_CM = 180.0;
  /** Alternate height for invalid BMI input (cm). */
  private static final double HEIGHT_INVALID_CM = 170.0;
  /** Alternate height used in BMR validation (cm). */
  private static final double HEIGHT_ALTERNATE_CM = 165.0;
  /** Extremely large height (cm). */
  private static final double HEIGHT_EXTREME_CM = 10000.0;
  /** Expectation for typical BMI result. */
  private static final double BMI_TYPICAL = 22.86;
  /** Expectation for underweight BMI result. */
  private static final double BMI_UNDER = 15.43;
  /** Expectation for overweight BMI result. */
  private static final double BMI_OVER = 26.82;
  /** Expectation for obese BMI result. */
  private static final double BMI_OBESE = 38.06;
  /** BMR expectation for male adult sample. */
  private static final double BMR_MALE_EXPECTED = 1853.632;
  /** BMR expectation for newborn female sample. */
  private static final double BMR_NEWBORN_FEMALE_EXPECTED = 1405.623;
  /** Calorie expectation for moderate activity. */
  private static final double CALORIES_MODERATE = 2480.0;
  /** Calorie expectation for light activity. */
  private static final double CALORIES_LIGHT = 2200.0;
  /** Calorie expectation for very active scenario. */
  private static final double CALORIES_VERY_ACTIVE = 2932.5;
  /** Calorie expectation for sedentary baseline. */
  private static final double CALORIES_SEDENTARY = 2160.0;
  /** Calorie expectation for extra active baseline. */
  private static final double CALORIES_EXTRA_ACTIVE = 3230.0;
  /** Sample BMR used in calorie tests. */
  private static final double BMR_SAMPLE = 1600.0;
  /** Alternate BMR used in calorie tests. */
  private static final double BMR_ALTERNATE = 1700.0;
  /** Higher BMR used in calorie tests. */
  private static final double BMR_HIGH = 1800.0;
  /** Additional BMR used for invalid frequency tests. */
  private static final double BMR_INVALID = 1500.0;
  /** Typical adult age in years. */
  private static final int AGE_THIRTY = 30;
  /** Age used for invalid BMR scenario. */
  private static final int AGE_THIRTY_FIVE = 35;
  /** Age delta for date math. */
  private static final int AGE_TWENTY_NINE = 29;
  /** Newborn age constant. */
  private static final int AGE_NEWBORN = 0;
  /** Number of days used for date math. */
  private static final int DAYS_TEN = 10;
  /** Training frequency for sedentary baseline. */
  private static final int TRAINING_FREQ_ZERO = 0;
  /** Training frequency for light activity. */
  private static final int TRAINING_FREQ_LIGHT = 2;
  /** Training frequency for moderate activity. */
  private static final int TRAINING_FREQ_MODERATE = 3;
  /** Training frequency for very active scenario. */
  private static final int TRAINING_FREQ_VERY_ACTIVE = 5;
  /** Training frequency for extra active scenario. */
  private static final int TRAINING_FREQ_EXTRA_ACTIVE = 7;
  /** Training frequency used for invalid scenario. */
  private static final int TRAINING_FREQ_INVALID = 4;

  /** Service instance under test. */
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
      assertEquals(expectedValue, result, BMI_ASSERT_TOLERANCE, description);
    }
  }

  private static Stream<Arguments> calculateBmiScenarios() {
    return Stream.of(
        Arguments.of(
            "Valid: typical adult metrics",
            WEIGHT_TYPICAL_KG,
            HEIGHT_TYPICAL_CM,
            BMI_TYPICAL,
            false),
        Arguments.of(
            "Boundary: underweight classification",
            WEIGHT_UNDER_KG,
            HEIGHT_TALL_CM,
            BMI_UNDER,
            false),
        Arguments.of(
            "Boundary: overweight classification",
            WEIGHT_OVER_KG,
            HEIGHT_OVER_CM,
            BMI_OVER,
            false),
        Arguments.of(
            "Boundary: obese classification",
            WEIGHT_OBESE_KG,
            HEIGHT_SHORT_CM,
            BMI_OBESE,
            false)
    );
  }

  @Test
  @DisplayName("calculateBMI throws for null inputs and invalid ranges")
  void calculateBmiInvalidInputs() {
    // null inputs
    Assertions.assertThrows(
        ResponseStatusException.class, () -> personService.calculateBMI(null, HEIGHT_INVALID_CM));
    Assertions.assertThrows(
        ResponseStatusException.class, () -> personService.calculateBMI(WEIGHT_TYPICAL_KG, null));

    // non-positive
    Assertions.assertThrows(
        ResponseStatusException.class,
        () -> personService.calculateBMI(WEIGHT_INVALID_KG, HEIGHT_INVALID_CM));
    Assertions.assertThrows(
        ResponseStatusException.class,
        () -> personService.calculateBMI(WEIGHT_NEGATIVE_KG, HEIGHT_INVALID_CM));

    // unreasonably large
    Assertions.assertThrows(
        ResponseStatusException.class,
        () -> personService.calculateBMI(WEIGHT_EXTREME_KG, HEIGHT_INVALID_CM));
    Assertions.assertThrows(
        ResponseStatusException.class,
        () -> personService.calculateBMI(WEIGHT_TYPICAL_KG, HEIGHT_EXTREME_CM));
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
            LocalDate.now().minusYears(AGE_TWENTY_NINE).minusDays(DAYS_TEN),
            null,
            false),
        Arguments.of("Boundary: born today -> age 0", LocalDate.now(), AGE_NEWBORN, false),
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
      assertEquals(expectedValue, result, CALORIE_ASSERT_TOLERANCE, description);
    }
  }

  private static Stream<Arguments> calculateBmrScenarios() {
    return Stream.of(
        Arguments.of(
            "Valid: male adult",
            WEIGHT_MALE_KG,
            HEIGHT_MALE_CM,
            AGE_THIRTY,
            true,
            BMR_MALE_EXPECTED,
            false),
        Arguments.of(
            "Boundary: newborn female",
            WEIGHT_NEWBORN_FEMALE_KG,
            HEIGHT_NEWBORN_FEMALE_CM,
            AGE_NEWBORN,
            false,
            BMR_NEWBORN_FEMALE_EXPECTED,
            false),
        Arguments.of(
            "Invalid: missing weight",
            null,
            HEIGHT_ALTERNATE_CM,
            AGE_THIRTY_FIVE,
            true,
            null,
            true));
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
      assertEquals(expectedValue, result, CALORIE_ASSERT_TOLERANCE, description);
    }
  }

  private static Stream<Arguments> calculateDailyCalorieNeedsScenarios() {
    return Stream.of(
        Arguments.of(
            "Valid: moderate activity",
            BMR_SAMPLE,
            TRAINING_FREQ_MODERATE,
            CALORIES_MODERATE,
            false),
        Arguments.of(
            "Valid: light activity",
            BMR_SAMPLE,
            TRAINING_FREQ_LIGHT,
            CALORIES_LIGHT,
            false),
        Arguments.of(
            "Valid: very active",
            BMR_ALTERNATE,
            TRAINING_FREQ_VERY_ACTIVE,
            CALORIES_VERY_ACTIVE,
            false),
        Arguments.of(
            "Boundary: sedentary baseline",
            BMR_HIGH,
            TRAINING_FREQ_ZERO,
            CALORIES_SEDENTARY,
            false),
        Arguments.of(
            "Boundary: extra active baseline",
            BMR_ALTERNATE,
            TRAINING_FREQ_EXTRA_ACTIVE,
            CALORIES_EXTRA_ACTIVE,
            false),
        Arguments.of("Invalid: missing BMR", null, TRAINING_FREQ_INVALID, null, true),
        Arguments.of("Invalid: missing frequency", BMR_INVALID, null, null, true));
  }
}
