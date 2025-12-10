package com.teamx.fitness.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.teamx.fitness.controller.dto.GoalPlanRequest;
import com.teamx.fitness.controller.dto.PersonCreateRequest;
import com.teamx.fitness.controller.dto.PersonCreatedResponse;
import com.teamx.fitness.controller.dto.PersonProfileResponse;
import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.model.PlanStrategy;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.HealthInsightResult;
import com.teamx.fitness.service.HealthInsightService;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * Focused unit tests for {@link PersonController} covering core behavior with mocked collaborators.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PersonController")
class PersonControllerTest {

  /** Standard adult weight used in BMI samples (kg). */
  private static final double WEIGHT_STANDARD_KG = 70.0;
  /** Underweight adult sample weight (kg). */
  private static final double WEIGHT_UNDER_KG = 50.0;
  /** Overweight adult sample weight (kg). */
  private static final double WEIGHT_OVER_KG = 85.0;
  /** Obese adult sample weight (kg). */
  private static final double WEIGHT_OBESE_KG = 110.0;
  /** Baseline persisted profile weight (kg). */
  private static final double WEIGHT_BASE_KG = 80.0;
  /** Updated profile weight (kg). */
  private static final double WEIGHT_UPDATED_KG = 82.0;
  /** Standard adult height used in BMI samples (cm). */
  private static final double HEIGHT_STANDARD_CM = 175.0;
  /** Tall adult height sample (cm). */
  private static final double HEIGHT_TALL_CM = 180.0;
  /** Slightly above-average adult height (cm). */
  private static final double HEIGHT_OVER_CM = 178.0;
  /** Shorter adult height sample (cm). */
  private static final double HEIGHT_SHORT_CM = 170.0;
  /** Updated profile height (cm). */
  private static final double HEIGHT_UPDATED_CM = 181.0;
  /** Expected BMI for the standard sample. */
  private static final double BMI_NORMAL = 22.86;
  /** Expected BMI for the underweight sample. */
  private static final double BMI_UNDER = 15.43;
  /** Expected BMI for the overweight sample. */
  private static final double BMI_OVER = 26.82;
  /** Expected BMI for the obese sample. */
  private static final double BMI_OBESE = 38.06;
  /** BMI returned during person creation happy-path. */
  private static final double BMI_RESPONSE = 24.22;
  /** BMI returned during update flows. */
  private static final double BMI_UPDATED = 24.0;
  /** Default target change for plan configuration (kg). */
  private static final double TARGET_CHANGE_PLAN_KG = 2.0;
  /** Target change used in update scenarios (kg). */
  private static final double TARGET_CHANGE_UPDATE_KG = 3.0;
  /** Target change used when configuring plans (kg). */
  private static final double TARGET_CHANGE_CONFIG_KG = 2.5;
  /** Expected maintenance calories for standard sample. */
  private static final double MAINTENANCE_CALORIES = 2500.0;
  /** Maintenance calories for sedentary scenario. */
  private static final double MAINTENANCE_CALORIES_SEDENTARY = 2016.0;
  /** Maintenance calories for high-activity scenario. */
  private static final double MAINTENANCE_CALORIES_ACTIVE = 2604.0;
  /** Default BMR returned from service. */
  private static final double BMR_STANDARD = 1680.0;
  /** Sample BMR used in calorie flows. */
  private static final double BMR_SAMPLE = 1600.0;
  /** BMR used when testing fallback logic. */
  private static final double BMR_FALLBACK = 1500.0;
  /** Calories associated with one kilogram change. */
  private static final double CALORIES_PER_KG = 7700.0;
  /** Days per week constant for averaging. */
  private static final double DAYS_PER_WEEK = 7.0;
  /** Negative weight used for validation test cases (kg). */
  private static final double NEGATIVE_WEIGHT_KG = -10.0;
  /** Target change for bulk-oriented recommendations (kg). */
  private static final double TARGET_CHANGE_BULK_KG = 3.0;
  /** Age used across calorie-related tests. */
  private static final int AGE_THIRTY = 30;
  /** Zero training-frequency placeholder. */
  private static final int TRAINING_FREQ_ZERO = 0;
  /** Light training frequency. */
  private static final int TRAINING_FREQ_THREE = 3;
  /** Standard training frequency. */
  private static final int TRAINING_FREQ_FOUR = 4;
  /** Heavy training frequency. */
  private static final int TRAINING_FREQ_FIVE = 5;
  /** Extreme training frequency used for validation. */
  private static final int TRAINING_FREQ_FOURTEEN = 14;
  /** Default target duration for plans (weeks). */
  private static final int TRAINING_DURATION_WEEKS = 6;
  /** Extended duration used in update tests (weeks). */
  private static final int TRAINING_DURATION_EXTENDED = 10;
  /** Persisted plan duration when the user has a plan (weeks). */
  private static final int TARGET_DURATION_DEFAULT = 8;
  /** Sentinel target duration representing no plan. */
  private static final int TARGET_DURATION_NONE = 0;
  /** Year constant used for historical DOBs (1985). */
  private static final int YEAR_1985 = 1985;
  /** Year constant used for historical DOBs (1988). */
  private static final int YEAR_1988 = 1988;
  /** Year constant used for historical DOBs (1990). */
  private static final int YEAR_1990 = 1990;
  /** Year constant used for historical DOBs (1992). */
  private static final int YEAR_1992 = 1992;
  /** First day of month value. */
  private static final int DAY_ONE = 1;
  /** Second day of month value. */
  private static final int DAY_TWO = 2;
  /** Third day of month value. */
  private static final int DAY_THREE = 3;
  /** Fourth day of month value. */
  private static final int DAY_FOUR = 4;
  /** Fifth day of month value. */
  private static final int DAY_FIVE = 5;

  /** Baseline DOB used for persisted profiles. */
  private static final LocalDate DOB_1990_JAN =
      LocalDate.of(YEAR_1990, Month.JANUARY, DAY_ONE);
  /** Alternate DOB used for female samples. */
  private static final LocalDate DOB_1992_FEB =
      LocalDate.of(YEAR_1992, Month.FEBRUARY, DAY_TWO);
  /** Alternate DOB used for overweight samples. */
  private static final LocalDate DOB_1988_MAR =
      LocalDate.of(YEAR_1988, Month.MARCH, DAY_THREE);
  /** Alternate DOB used for obese samples. */
  private static final LocalDate DOB_1985_APR =
      LocalDate.of(YEAR_1985, Month.APRIL, DAY_FOUR);
  /** DOB used when verifying update flows. */
  private static final LocalDate DOB_1990_MAY =
      LocalDate.of(YEAR_1990, Month.MAY, DAY_FIVE);
  /** BMI returned by mocked health insight service. */
  private static final double INSIGHT_BMI_VALUE = 26.4;
  /** BMI category returned by mocked health insight service. */
  private static final String INSIGHT_BMI_CATEGORY = "Overweight";
  /** Health index returned by mocked health insight service. */
  private static final double INSIGHT_HEALTH_INDEX = 58.0;
  /** Plan alignment score returned by mocked health insight service. */
  private static final double INSIGHT_PLAN_SCORE = 54.0;
  /** Overall score returned by mocked health insight service. */
  private static final double INSIGHT_OVERALL_SCORE = 56.0;
  /** Percentile returned by mocked health insight service. */
  private static final double INSIGHT_PERCENTILE = 67.4;

  /** Mocked BMI/calorie service. */
  @Mock private PersonService personService;

  /** Mocked persistence layer. */
  @Mock private PersonRepository personRepository;

  /** Mocked insight service powering /recommendation. */
  @Mock private HealthInsightService healthInsightService;

  /** Controller instance under test. */
  @InjectMocks private PersonController personController;

  @AfterEach
  void clearClientContext() {
    ClientContext.clear();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("bmiScenarios")
  @DisplayName("calculateBMI uses stored profile data")
  void calculateBMIUsesStoredData(
      String description,
      PersonSimple storedPerson,
      Double bmiValue,
      String expectedCategory) {

    ClientContext.setClientId(storedPerson.getClientId());
    when(personRepository.findByClientId(storedPerson.getClientId()))
        .thenReturn(Optional.of(storedPerson));
    when(personService.calculateBMI(storedPerson.getWeight(), storedPerson.getHeight()))
        .thenReturn(bmiValue);

    ResponseEntity<Map<String, Object>> response = personController.calculateBMI();

    assertEquals(HttpStatus.OK, response.getStatusCode(), description);
    Map<String, Object> body = response.getBody();
    assertNotNull(body, description);
    assertEquals(storedPerson.getClientId(), body.get("clientId"));
    assertEquals(storedPerson.getWeight(), body.get("weight"));
    assertEquals(storedPerson.getHeight(), body.get("height"));
    assertEquals(bmiValue, body.get("bmi"));
    assertEquals(expectedCategory, body.get("category"));
  }

  private static Stream<Arguments> bmiScenarios() {
    PersonSimple normal =
        new PersonSimple(
            "Normal",
            WEIGHT_STANDARD_KG,
            HEIGHT_STANDARD_CM,
            DOB_1990_JAN,
            Gender.MALE,
            FitnessGoal.CUT,
            "mobile-normal");
    PersonSimple underweight =
        new PersonSimple(
            "Under",
            WEIGHT_UNDER_KG,
            HEIGHT_TALL_CM,
            DOB_1992_FEB,
            Gender.FEMALE,
            FitnessGoal.BULK,
            "mobile-under");
    PersonSimple overweight =
        new PersonSimple(
            "Over",
            WEIGHT_OVER_KG,
            HEIGHT_OVER_CM,
            DOB_1988_MAR,
            Gender.MALE,
            FitnessGoal.CUT,
            "mobile-over");
    PersonSimple obese =
        new PersonSimple(
            "Obese",
            WEIGHT_OBESE_KG,
            HEIGHT_SHORT_CM,
            DOB_1985_APR,
            Gender.FEMALE,
            FitnessGoal.BULK,
            "mobile-obese");
    return Stream.of(
        Arguments.of("Normal category", normal, BMI_NORMAL, "Normal weight"),
        Arguments.of("Underweight category", underweight, BMI_UNDER, "Underweight"),
        Arguments.of("Overweight category", overweight, BMI_OVER, "Overweight"),
        Arguments.of("Obese category", obese, BMI_OBESE, "Obese"),
        Arguments.of("Unknown BMI when service returns null", normal, null, "Unknown"));
  }

  @Test
  @DisplayName("calculateBMI throws 404 when profile not found")
  void calculateBMIThrowsWhenProfileMissing() {
    ClientContext.setClientId("mobile-missing");
    when(personRepository.findByClientId("mobile-missing")).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> personController.calculateBMI());
  }

  @Test
  @DisplayName("createPerson rejects birthDate equal to today")
  void createPersonRejectsBirthDateToday() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("Test");
    request.setWeight(WEIGHT_STANDARD_KG);
    request.setHeight(HEIGHT_SHORT_CM);
    request.setBirthDate(LocalDate.now());
    request.setGoal(FitnessGoal.CUT);
    request.setGender(Gender.MALE);

    when(personService.calculateBMI(WEIGHT_STANDARD_KG, HEIGHT_SHORT_CM)).thenReturn(BMI_RESPONSE);

    assertThrows(ResponseStatusException.class, () -> personController.createPerson(request));
  }

  @Test
  @DisplayName("createPerson rejects invalid weight")
  void createPersonRejectsInvalidWeight() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("Test2");
    request.setWeight(NEGATIVE_WEIGHT_KG);
    request.setHeight(HEIGHT_SHORT_CM);
    request.setBirthDate(DOB_1990_JAN);
    request.setGoal(FitnessGoal.BULK);
    request.setGender(Gender.FEMALE);

    when(personService.calculateBMI(NEGATIVE_WEIGHT_KG, HEIGHT_SHORT_CM))
        .thenThrow(
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "weight must be greater than 0"));

    assertThrows(ResponseStatusException.class, () -> personController.createPerson(request));
  }

  @Test
  @DisplayName("createPerson rejects missing gender selection")
  void createPersonRejectsMissingGender() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("NoGender");
    request.setWeight(WEIGHT_STANDARD_KG);
    request.setHeight(HEIGHT_SHORT_CM);
    request.setBirthDate(DOB_1990_JAN);
    request.setGoal(FitnessGoal.CUT);
    request.setGender(null);

    when(personService.calculateBMI(WEIGHT_STANDARD_KG, HEIGHT_SHORT_CM)).thenReturn(BMI_RESPONSE);

    assertThrows(ResponseStatusException.class, () -> personController.createPerson(request));
  }

  @Test
  @DisplayName("createPerson returns generated client identifier")
  void createPersonReturnsClientId() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("Valid");
    request.setWeight(WEIGHT_STANDARD_KG);
    request.setHeight(HEIGHT_SHORT_CM);
    request.setBirthDate(DOB_1990_JAN);
    request.setGoal(FitnessGoal.CUT);
    request.setGender(Gender.MALE);

    when(personService.calculateBMI(WEIGHT_STANDARD_KG, HEIGHT_SHORT_CM)).thenReturn(BMI_RESPONSE);
    lenient().when(personRepository.findByClientId(anyString())).thenReturn(Optional.empty());
    when(personRepository.save(any(PersonSimple.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, PersonSimple.class));

    ResponseEntity<PersonCreatedResponse> response = personController.createPerson(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    PersonCreatedResponse body = response.getBody();
    assertNotNull(body);
    assertNotNull(body.getClientId());
    assertTrue(body.getClientId().startsWith(ClientContext.MOBILE_PREFIX));
  }

  @Test
  @DisplayName("getProfile returns persisted data")
  void getProfileReturnsData() {
    PersonSimple stored = basePerson("mobile-profile");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<PersonProfileResponse> response = personController.getProfile();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(stored.getClientId(), response.getBody().getClientId());
  }

  @Test
  @DisplayName("getProfile requires X-Client-ID header")
  void getProfileRequiresHeader() {
    ClientContext.clear();
    assertThrows(ResponseStatusException.class, () -> personController.getProfile());
  }

  @Test
  @DisplayName("configureGoalPlan updates plan fields")
  void configureGoalPlanUpdatesPlan() {
    PersonSimple stored = basePerson("mobile-plan");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(inv -> inv.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(TARGET_CHANGE_CONFIG_KG);
    request.setDurationWeeks(TRAINING_DURATION_WEEKS);
    request.setTrainingFrequencyPerWeek(TRAINING_FREQ_FOUR);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ArgumentCaptor<PersonSimple> captor = ArgumentCaptor.forClass(PersonSimple.class);
    verify(personRepository).save(captor.capture());
    PersonSimple saved = captor.getValue();
    assertEquals(TARGET_CHANGE_CONFIG_KG, saved.getTargetChangeKg());
    assertEquals(TRAINING_DURATION_WEEKS, saved.getTargetDurationWeeks());
    assertEquals(TRAINING_FREQ_FOUR, saved.getTrainingFrequencyPerWeek());
    assertEquals(PlanStrategy.BOTH, saved.getPlanStrategy());
  }

  @Test
  @DisplayName("configureGoalPlan requires goal and mandatory fields")
  void configureGoalPlanRequiresData() {
    PersonSimple stored = basePerson("mobile-plan-missing");
    stored.setGoal(null);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(null);

    assertThrows(ResponseStatusException.class, () -> personController.configureGoalPlan(request));
  }

  @Test
  @DisplayName("configureGoalPlan enforces all fields when goal exists")
  void configureGoalPlanRequiresAllFields() {
    PersonSimple stored = basePerson("mobile-plan-fields");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(TARGET_CHANGE_PLAN_KG);
    request.setDurationWeeks(null);
    request.setTrainingFrequencyPerWeek(TRAINING_FREQ_THREE);
    request.setPlanStrategy(PlanStrategy.DIET);

    assertThrows(ResponseStatusException.class, () -> personController.configureGoalPlan(request));
  }

  @Test
  @DisplayName("updatePerson trims name and persists new metrics")
  void updatePersonTrimsAndUpdatesMetrics() {
    PersonSimple stored = basePerson("mobile-update");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(inv -> inv.getArgument(0));
    when(personService.calculateBMI(anyDouble(), anyDouble())).thenReturn(BMI_UPDATED);

    PersonSimple update = new PersonSimple();
    update.setName("  Updated User  ");
    update.setWeight(WEIGHT_UPDATED_KG);
    update.setHeight(HEIGHT_UPDATED_CM);
    update.setBirthDate(DOB_1990_MAY);
    update.setGender(Gender.MALE);
    update.setGoal(FitnessGoal.BULK);
    update.setTargetChangeKg(TARGET_CHANGE_UPDATE_KG);
    update.setTargetDurationWeeks(TRAINING_DURATION_EXTENDED);
    update.setTrainingFrequencyPerWeek(TRAINING_FREQ_FIVE);
    update.setPlanStrategy(PlanStrategy.WORKOUT);

    ResponseEntity<PersonSimple> response = personController.updatePerson(update);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    PersonSimple saved = response.getBody();
    assertNotNull(saved);
    assertEquals("Updated User", saved.getName());
    assertEquals(WEIGHT_UPDATED_KG, saved.getWeight());
    assertEquals(PlanStrategy.WORKOUT, saved.getPlanStrategy());
  }

  @Test
  @DisplayName("updatePerson rejects missing gender")
  void updatePersonRejectsMissingGender() {
    PersonSimple stored = basePerson("mobile-update-missing-gender");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personService.calculateBMI(stored.getWeight(), stored.getHeight())).thenReturn(BMI_UPDATED);

    PersonSimple update = basePerson("mobile-update-missing-gender");
    update.setGender(null);

    assertThrows(ResponseStatusException.class, () -> personController.updatePerson(update));
  }

  @Test
  @DisplayName("updatePerson rejects invalid birth date")
  void updatePersonRejectsFutureBirthDate() {
    PersonSimple stored = basePerson("mobile-update-invalid");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    PersonSimple update = basePerson("mobile-update-invalid");
    update.setBirthDate(LocalDate.now().plusDays(1));

    assertThrows(ResponseStatusException.class, () -> personController.updatePerson(update));
  }

  @Test
  @DisplayName("deletePerson returns confirmation payload")
  void deletePersonReturnsMessage() {
    PersonSimple stored = basePerson("mobile-delete");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, String>> response = personController.deletePerson();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Profile deleted successfully", response.getBody().get("message"));
    verify(personRepository).delete(stored);
  }

  @Test
  @DisplayName("deletePerson returns 404 when profile missing")
  void deletePersonReturnsNotFound() {
    ClientContext.setClientId("mobile-missing");
    when(personRepository.findByClientId("mobile-missing")).thenReturn(Optional.empty());

    ResponseEntity<Map<String, String>> response = personController.deletePerson();

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(personRepository, never()).delete(any());
  }

  @Test
  @DisplayName("calculateDailyCalories returns nutritional guidance")
  void calculateDailyCaloriesReturnsGuidance() {
    PersonSimple stored = basePerson("mobile-calories");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personService.calculateAge(stored.getBirthDate())).thenReturn(AGE_THIRTY);
    when(personService.calculateBMR(stored.getWeight(), stored.getHeight(), AGE_THIRTY, true))
        .thenReturn(BMR_SAMPLE);
    when(personService.calculateDailyCalorieNeeds(BMR_SAMPLE, stored.getTrainingFrequencyPerWeek()))
        .thenReturn(MAINTENANCE_CALORIES);

    ResponseEntity<Map<String, Object>> response = personController.calculateDailyCalories();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> body = response.getBody();
    assertEquals(FitnessGoal.CUT, body.get("goal"));
    assertEquals(MAINTENANCE_CALORIES, body.get("maintenanceCalories"));
  }

  @Test
  @DisplayName("calculateDailyCalories handles negative target changes")
  void calculateDailyCaloriesHandlesNegativeTargetChange() {
    PersonSimple stored = basePerson("mobile-calories-negative");
    stored.setTargetChangeKg(-TARGET_CHANGE_PLAN_KG);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personService.calculateAge(stored.getBirthDate())).thenReturn(AGE_THIRTY);
    when(personService.calculateBMR(stored.getWeight(), stored.getHeight(), AGE_THIRTY, true))
        .thenReturn(BMR_SAMPLE);
    when(personService.calculateDailyCalorieNeeds(BMR_SAMPLE, stored.getTrainingFrequencyPerWeek()))
        .thenReturn(MAINTENANCE_CALORIES);

    ResponseEntity<Map<String, Object>> response = personController.calculateDailyCalories();

    Map<String, Object> body = response.getBody();
    double adjustment = Math.abs(
        stored.getTargetChangeKg() * CALORIES_PER_KG
            / stored.getTargetDurationWeeks()
            / DAYS_PER_WEEK);
    assertEquals(-adjustment, (Double) body.get("calorieAdjustmentPerDay"));
    assertTrue((Double) body.get("recommendedDailyCalories") < MAINTENANCE_CALORIES);
  }

  @Test
  @DisplayName("calculateDailyCalories requires training frequency")
  void calculateDailyCaloriesRequiresFrequency() {
    PersonSimple stored = basePerson("mobile-calories-frequency");
    stored.setTrainingFrequencyPerWeek(null);
    expectCalorieFailure(stored);
  }

  @Test
  @DisplayName("calculateDailyCalories requires plan strategy and targets")
  void calculateDailyCaloriesRequiresPlanStrategy() {
    PersonSimple stored = basePerson("mobile-calories-plan");
    stored.setPlanStrategy(null);
    expectCalorieFailure(stored);
  }

  @Test
  @DisplayName("calculateDailyCalories requires target values")
  void calculateDailyCaloriesRequiresTargets() {
    PersonSimple stored = basePerson("mobile-calories-target");
    stored.setTargetChangeKg(null);
    expectCalorieFailure(stored);
  }

  @Test
  @DisplayName("calculateDailyCalories requires positive duration")
  void calculateDailyCaloriesRequiresPositiveDuration() {
    PersonSimple stored = basePerson("mobile-calories-duration");
    stored.setTargetDurationWeeks(TARGET_DURATION_NONE);
    expectCalorieFailure(stored);
  }

  @Test
  @DisplayName("calculateDailyCalories requires age, BMR, and maintenance calorie data")
  void calculateDailyCaloriesNullDownstreamValues() {
    PersonSimple stored = basePerson("mobile-calories-age");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personService.calculateAge(stored.getBirthDate())).thenReturn(null);
    assertThrows(ResponseStatusException.class, () -> personController.calculateDailyCalories());

    when(personService.calculateAge(stored.getBirthDate())).thenReturn(AGE_THIRTY);
    when(personService.calculateBMR(stored.getWeight(), stored.getHeight(), AGE_THIRTY, true))
        .thenReturn(null);
    assertThrows(ResponseStatusException.class, () -> personController.calculateDailyCalories());

    when(personService.calculateBMR(stored.getWeight(), stored.getHeight(), AGE_THIRTY, true))
        .thenReturn(BMR_FALLBACK);
    when(personService.calculateDailyCalorieNeeds(BMR_FALLBACK, stored.getTrainingFrequencyPerWeek()))
        .thenReturn(null);
    assertThrows(ResponseStatusException.class, () -> personController.calculateDailyCalories());
  }

  @Test
  @DisplayName("provideRecommendation surfaces insight metrics")
  void provideRecommendationIncludesInsightFields() {
    PersonSimple stored = basePerson("mobile-reco");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Stay the course."));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    assertNotNull(body);
    assertEquals("Stay the course.", body.get("message"));
    assertEquals(INSIGHT_BMI_VALUE, body.get("bmi"));
    assertEquals(INSIGHT_BMI_CATEGORY, body.get("bmiCategory"));
    assertEquals(INSIGHT_HEALTH_INDEX, body.get("healthIndex"));
    assertEquals(INSIGHT_PLAN_SCORE, body.get("planAlignmentIndex"));
    assertEquals(INSIGHT_OVERALL_SCORE, body.get("overallScore"));
    assertEquals(INSIGHT_PERCENTILE, body.get("percentile"));
  }

  @Test
  @DisplayName("provideRecommendation omits plan alignment when insight is missing")
  void provideRecommendationOmitsPlanAlignmentWhenNull() {
    PersonSimple stored = basePerson("mobile-reco-noplan");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(new HealthInsightResult(
            INSIGHT_BMI_VALUE,
            INSIGHT_BMI_CATEGORY,
            INSIGHT_HEALTH_INDEX,
            null,
            INSIGHT_HEALTH_INDEX,
            null,
            null,
            "Plan pending"));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    assertNotNull(body);
    assertNull(body.get("planAlignmentIndex"), "planAlignmentIndex should be null when insight omits it");
    assertEquals(INSIGHT_HEALTH_INDEX, body.get("overallScore"), "Overall score should fall back to health index");
  }

  @Test
  @DisplayName("provideRecommendation includes cohort warning when present")
  void provideRecommendationIncludesCohortWarning() {
    PersonSimple stored = basePerson("mobile-reco-warning");
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Need more data", "Cohort too small"));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    assertEquals("Cohort too small", body.get("cohortWarning"));
  }

  @Test
  @DisplayName("provideRecommendation throws error when plan details are missing even with goal null")
  void provideRecommendationGeneratesDietPlanForUnknownGoal() {
    PersonSimple stored = basePerson("mobile-reco-diet-unknown");
    stored.setGoal(null);
    stored.setPlanStrategy(PlanStrategy.DIET);
    stored.setTargetChangeKg(null);
    stored.setTargetDurationWeeks(null);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.provideRecommendation());

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("All goal plan fields must be configured"));
  }

  @Test
  @DisplayName("provideRecommendation clamps workout frequency to minimum value")
  void provideRecommendationClampsWorkoutFrequency() {
    PersonSimple stored = basePerson("mobile-reco-workout-min");
    stored.setGoal(null);
    stored.setPlanStrategy(PlanStrategy.WORKOUT);
    stored.setTrainingFrequencyPerWeek(0);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Workout message"));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    String workoutPlan = (String) response.getBody().get("workoutPlan");
    assertTrue(workoutPlan.contains("Schedule 1 total-body"), "Workout plan should clamp to minimum frequency");
  }

  @Test
  @DisplayName("provideRecommendation adds diet plan text for diet strategies")
  void provideRecommendationIncludesDietPlanDetails() {
    PersonSimple stored = basePerson("mobile-reco-diet");
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Diet focus message"));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    assertNotNull(body.get("dietPlan"));
    assertEquals("Diet focus message", body.get("message"));
  }

  @Test
  @DisplayName("provideRecommendation adds workout plan text for workout strategies")
  void provideRecommendationIncludesWorkoutPlanDetails() {
    PersonSimple stored = basePerson("mobile-reco-workout");
    stored.setPlanStrategy(PlanStrategy.WORKOUT);
    stored.setTrainingFrequencyPerWeek(TRAINING_FREQ_FIVE);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Workout focus message"));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    assertNotNull(body.get("workoutPlan"));
    assertEquals("Workout focus message", body.get("message"));
  }

  @Test
  @DisplayName("provideRecommendation throws error when plan details are missing")
  void provideRecommendationOmitsPlanDetailsWhenMissing() {
    PersonSimple stored = basePerson("mobile-reco-no-plan");
    stored.setPlanStrategy(null);
    stored.setTrainingFrequencyPerWeek(null);
    stored.setTargetChangeKg(null);
    stored.setTargetDurationWeeks(null);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.provideRecommendation());

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("All goal plan fields must be configured"));
  }

  @Test
  @DisplayName("createPerson retries client ID generation when collisions occur")
  void createPersonRetriesClientIdGeneration() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("Retry");
    request.setWeight(WEIGHT_STANDARD_KG);
    request.setHeight(HEIGHT_SHORT_CM);
    request.setBirthDate(DOB_1990_JAN);
    request.setGoal(FitnessGoal.CUT);
    request.setGender(Gender.MALE);

    when(personService.calculateBMI(WEIGHT_STANDARD_KG, HEIGHT_SHORT_CM)).thenReturn(BMI_RESPONSE);
    when(personRepository.findByClientId("mobile-id1"))
        .thenReturn(Optional.of(new PersonSimple()));
    when(personRepository.findByClientId("mobile-id2")).thenReturn(Optional.empty());
    when(personRepository.save(any(PersonSimple.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, PersonSimple.class));

    ResponseEntity<PersonCreatedResponse> response = personController.createPerson(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    PersonCreatedResponse body = response.getBody();
    assertNotNull(body);
    assertEquals("mobile-id2", body.getClientId());
  }

  @Test
  @DisplayName("getProfile rejects blank client identifier")
  void getProfileRejectsBlankClientId() {
    ClientContext.setClientId("  ");
    assertThrows(ResponseStatusException.class, () -> personController.getProfile());
  }

  private void expectCalorieFailure(PersonSimple stored) {
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    assertThrows(ResponseStatusException.class, () -> personController.calculateDailyCalories());
  }

  private HealthInsightResult sampleInsight(String message) {
    return sampleInsight(message, null);
  }

  private HealthInsightResult sampleInsight(String message, String warning) {
    return new HealthInsightResult(
        INSIGHT_BMI_VALUE,
        INSIGHT_BMI_CATEGORY,
        INSIGHT_HEALTH_INDEX,
        INSIGHT_PLAN_SCORE,
        INSIGHT_OVERALL_SCORE,
        INSIGHT_PERCENTILE,
        warning,
        message);
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for CUT - below minimum weight")
  void configureGoalPlanValidatesTargetWeightCutBelowMinimum() {
    PersonSimple stored = basePerson("mobile-target-weight-cut");
    stored.setWeight(35.0); // Current weight
    stored.setGoal(FitnessGoal.CUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(10.0); // Would result in 25kg target weight (< 30kg)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.configureGoalPlan(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("below the minimum healthy weight"));
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for CUT - below minimum BMI")
  void configureGoalPlanValidatesTargetWeightCutBelowMinimumBMI() {
    PersonSimple stored = basePerson("mobile-target-weight-cut-bmi");
    stored.setWeight(50.0);
    stored.setHeight(200.0); // Tall person
    stored.setGoal(FitnessGoal.CUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    GoalPlanRequest request = new GoalPlanRequest();
    // Target weight = 50 - 20 = 30kg (passes weight check), BMI = 30/(2.0^2) = 7.5 (< 15, fails BMI check)
    request.setTargetChangeKg(20.0); // Would result in 30kg (at minimum weight), BMI = 7.5 (< 15)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.configureGoalPlan(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("below the minimum healthy BMI"));
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for CUT - valid weight")
  void configureGoalPlanValidatesTargetWeightCutValid() {
    PersonSimple stored = basePerson("mobile-target-weight-cut-valid");
    stored.setWeight(70.0);
    stored.setHeight(175.0);
    stored.setGoal(FitnessGoal.CUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(5.0); // Would result in 65kg target weight (valid)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for BULK - above maximum weight")
  void configureGoalPlanValidatesTargetWeightBulkAboveMaximum() {
    PersonSimple stored = basePerson("mobile-target-weight-bulk");
    stored.setWeight(195.0); // Current weight
    stored.setGoal(FitnessGoal.BULK);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(10.0); // Would result in 205kg target weight (> 200kg)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.configureGoalPlan(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("exceeds the maximum reasonable weight"));
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for BULK - above maximum BMI")
  void configureGoalPlanValidatesTargetWeightBulkAboveMaximumBMI() {
    PersonSimple stored = basePerson("mobile-target-weight-bulk-bmi");
    stored.setWeight(80.0);
    stored.setHeight(150.0); // Shorter person
    stored.setGoal(FitnessGoal.BULK);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(50.0); // Would result in 130kg, BMI = 130/(1.5^2) = 57.8 (> 50)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.configureGoalPlan(request));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("exceed the maximum reasonable BMI"));
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for BULK - valid weight")
  void configureGoalPlanValidatesTargetWeightBulkValid() {
    PersonSimple stored = basePerson("mobile-target-weight-bulk-valid");
    stored.setWeight(70.0);
    stored.setHeight(175.0);
    stored.setGoal(FitnessGoal.BULK);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(5.0); // Would result in 75kg target weight (valid)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("configureGoalPlan skips target weight validation when weight is null")
  void configureGoalPlanSkipsValidationWhenWeightNull() {
    PersonSimple stored = basePerson("mobile-target-weight-null");
    stored.setWeight(null);
    stored.setGoal(FitnessGoal.CUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(5.0);
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    // Should not throw exception for weight validation, but weight is null so BMI check is skipped
    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("provideRecommendation returns diet plan with plan alignment 0")
  void provideRecommendationReturnsDietPlanWithAlignmentZero() {
    PersonSimple stored = basePerson("mobile-reco-alignment-zero");
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(new HealthInsightResult(
            INSIGHT_BMI_VALUE,
            INSIGHT_BMI_CATEGORY,
            INSIGHT_HEALTH_INDEX,
            0.0, // planAlignmentIndex = 0
            INSIGHT_OVERALL_SCORE,
            INSIGHT_PERCENTILE,
            null,
            "Test message"));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String dietPlan = (String) body.get("dietPlan");
    assertTrue(dietPlan.contains("Cannot provide a diet plan") || dietPlan.contains("Plan Alignment = 0"));
  }

  @Test
  @DisplayName("provideRecommendation returns diet plan with goal null")
  void provideRecommendationReturnsDietPlanWithGoalNull() {
    PersonSimple stored = basePerson("mobile-reco-goal-null");
    stored.setGoal(null);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String dietPlan = (String) body.get("dietPlan");
    assertTrue(dietPlan.contains("balanced meal plan"));
  }

  @Test
  @DisplayName("provideRecommendation returns diet plan for CUT with capped deficit")
  void provideRecommendationReturnsDietPlanCutWithCappedDeficit() {
    PersonSimple stored = basePerson("mobile-reco-cut-capped");
    stored.setGoal(FitnessGoal.CUT);
    stored.setTargetChangeKg(20.0); // Large target change
    stored.setTargetDurationWeeks(2); // Short duration -> high daily deficit
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String dietPlan = (String) body.get("dietPlan");
    assertTrue(dietPlan.contains("capped at maximum safe deficit") || dietPlan.contains("1500"));
  }

  @Test
  @DisplayName("provideRecommendation returns diet plan for CUT without capped deficit")
  void provideRecommendationReturnsDietPlanCutWithoutCappedDeficit() {
    PersonSimple stored = basePerson("mobile-reco-cut-normal");
    stored.setGoal(FitnessGoal.CUT);
    stored.setTargetChangeKg(2.0);
    stored.setTargetDurationWeeks(8);
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String dietPlan = (String) body.get("dietPlan");
    assertTrue(dietPlan.contains("kcal deficit per day"));
    assertFalse(dietPlan.contains("capped"));
  }

  @Test
  @DisplayName("provideRecommendation returns diet plan for BULK with capped surplus")
  void provideRecommendationReturnsDietPlanBulkWithCappedSurplus() {
    PersonSimple stored = basePerson("mobile-reco-bulk-capped");
    stored.setGoal(FitnessGoal.BULK);
    stored.setTargetChangeKg(15.0); // Large target change
    stored.setTargetDurationWeeks(2); // Short duration -> high daily surplus
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String dietPlan = (String) body.get("dietPlan");
    assertTrue(dietPlan.contains("capped at maximum safe surplus") || dietPlan.contains("1000"));
  }

  @Test
  @DisplayName("provideRecommendation returns diet plan for BULK without capped surplus")
  void provideRecommendationReturnsDietPlanBulkWithoutCappedSurplus() {
    PersonSimple stored = basePerson("mobile-reco-bulk-normal");
    stored.setGoal(FitnessGoal.BULK);
    stored.setTargetChangeKg(3.0);
    stored.setTargetDurationWeeks(12);
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String dietPlan = (String) body.get("dietPlan");
    assertTrue(dietPlan.contains("kcal surplus daily"));
    assertFalse(dietPlan.contains("capped"));
  }

  @Test
  @DisplayName("provideRecommendation requires all goal plan fields to be configured")
  void provideRecommendationRequiresAllGoalPlanFields() {
    PersonSimple stored = basePerson("mobile-reco-missing-target");
    stored.setGoal(FitnessGoal.CUT);
    stored.setTargetChangeKg(null); // Missing target change
    stored.setTargetDurationWeeks(8);
    stored.setTrainingFrequencyPerWeek(4);
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.provideRecommendation());

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("All goal plan fields must be configured"));
  }

  @Test
  @DisplayName("updatePerson validates target weight for CUT")
  void updatePersonValidatesTargetWeightCut() {
    PersonSimple stored = basePerson("mobile-update-cut-validation");
    stored.setWeight(35.0);
    stored.setGoal(FitnessGoal.CUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personService.calculateBMI(anyDouble(), anyDouble())).thenReturn(20.0);

    PersonSimple updatedPerson = new PersonSimple();
    updatedPerson.setWeight(35.0);
    updatedPerson.setHeight(175.0);
    updatedPerson.setGoal(FitnessGoal.CUT);
    updatedPerson.setTargetChangeKg(10.0); // Would result in 25kg (< 30kg)
    updatedPerson.setTargetDurationWeeks(10);
    updatedPerson.setTrainingFrequencyPerWeek(4);
    updatedPerson.setPlanStrategy(PlanStrategy.BOTH);
    updatedPerson.setName("Updated");
    updatedPerson.setGender(Gender.MALE);
    updatedPerson.setBirthDate(DOB_1990_JAN);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.updatePerson(updatedPerson));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("below the minimum healthy weight"));
  }

  @Test
  @DisplayName("updatePerson validates target weight for BULK")
  void updatePersonValidatesTargetWeightBulk() {
    PersonSimple stored = basePerson("mobile-update-bulk-validation");
    stored.setWeight(195.0);
    stored.setGoal(FitnessGoal.BULK);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personService.calculateBMI(anyDouble(), anyDouble())).thenReturn(30.0);

    PersonSimple updatedPerson = new PersonSimple();
    updatedPerson.setWeight(195.0);
    updatedPerson.setHeight(175.0);
    updatedPerson.setGoal(FitnessGoal.BULK);
    updatedPerson.setTargetChangeKg(10.0); // Would result in 205kg (> 200kg)
    updatedPerson.setTargetDurationWeeks(10);
    updatedPerson.setTrainingFrequencyPerWeek(4);
    updatedPerson.setPlanStrategy(PlanStrategy.BOTH);
    updatedPerson.setName("Updated");
    updatedPerson.setGender(Gender.MALE);
    updatedPerson.setBirthDate(DOB_1990_JAN);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.updatePerson(updatedPerson));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("exceeds the maximum reasonable weight"));
  }

  @Test
  @DisplayName("updatePerson skips target weight validation when target change is null")
  void updatePersonSkipsTargetWeightValidationWhenTargetChangeNull() {
    PersonSimple stored = basePerson("mobile-update-null-target");
    stored.setWeight(70.0);
    stored.setGoal(FitnessGoal.CUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personService.calculateBMI(anyDouble(), anyDouble())).thenReturn(22.0);
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    PersonSimple updatedPerson = new PersonSimple();
    updatedPerson.setWeight(70.0);
    updatedPerson.setHeight(175.0);
    updatedPerson.setGoal(FitnessGoal.CUT);
    updatedPerson.setTargetChangeKg(null); // No target change
    updatedPerson.setName("Updated");
    updatedPerson.setGender(Gender.MALE);
    updatedPerson.setBirthDate(DOB_1990_JAN);

    ResponseEntity<PersonSimple> response = personController.updatePerson(updatedPerson);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for CUT - skips BMI check when height is null")
  void configureGoalPlanValidatesTargetWeightCutSkipsBMIWhenHeightNull() {
    PersonSimple stored = basePerson("mobile-target-weight-cut-no-height");
    stored.setWeight(35.0);
    stored.setHeight(null); // No height
    stored.setGoal(FitnessGoal.CUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(5.0); // Would result in 30kg (exactly at minimum)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for CUT - skips BMI check when height is zero")
  void configureGoalPlanValidatesTargetWeightCutSkipsBMIWhenHeightZero() {
    PersonSimple stored = basePerson("mobile-target-weight-cut-height-zero");
    stored.setWeight(35.0);
    stored.setHeight(0.0); // Zero height
    stored.setGoal(FitnessGoal.CUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(5.0); // Would result in 30kg (exactly at minimum)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for BULK - skips BMI check when height is null")
  void configureGoalPlanValidatesTargetWeightBulkSkipsBMIWhenHeightNull() {
    PersonSimple stored = basePerson("mobile-target-weight-bulk-no-height");
    stored.setWeight(190.0);
    stored.setHeight(null); // No height
    stored.setGoal(FitnessGoal.BULK);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(5.0); // Would result in 195kg (below maximum)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for BULK - skips BMI check when height is zero")
  void configureGoalPlanValidatesTargetWeightBulkSkipsBMIWhenHeightZero() {
    PersonSimple stored = basePerson("mobile-target-weight-bulk-height-zero");
    stored.setWeight(190.0);
    stored.setHeight(0.0); // Zero height
    stored.setGoal(FitnessGoal.BULK);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(5.0); // Would result in 195kg (below maximum)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("configureGoalPlan requires goal to be set before configuring plan")
  void configureGoalPlanRequiresGoalBeforeConfiguringPlan() {
    PersonSimple stored = basePerson("mobile-target-weight-goal-null");
    stored.setWeight(35.0);
    stored.setGoal(null); // No goal
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(10.0);
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    // Should throw exception because goal is required
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.configureGoalPlan(request));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("A goal must be selected"));
  }

  @Test
  @DisplayName("provideRecommendation requires duration weeks to be configured")
  void provideRecommendationRequiresDurationWeeks() {
    PersonSimple stored = basePerson("mobile-reco-missing-duration");
    stored.setGoal(FitnessGoal.CUT);
    stored.setTargetChangeKg(2.0);
    stored.setTargetDurationWeeks(null); // Missing duration
    stored.setTrainingFrequencyPerWeek(4);
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.provideRecommendation());

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("All goal plan fields must be configured"));
  }

  @Test
  @DisplayName("provideRecommendation returns diet plan with zero duration weeks")
  void provideRecommendationReturnsDietPlanWithZeroDurationWeeks() {
    PersonSimple stored = basePerson("mobile-reco-zero-duration");
    stored.setGoal(FitnessGoal.CUT);
    stored.setTargetChangeKg(2.0);
    stored.setTargetDurationWeeks(0); // Zero duration (allowed, will use default adjustment)
    stored.setTrainingFrequencyPerWeek(4);
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String dietPlan = (String) body.get("dietPlan");
    // Should use default adjustment when duration is 0
    assertNotNull(dietPlan);
  }

  @Test
  @DisplayName("provideRecommendation returns workout plan with plan alignment 0")
  void provideRecommendationReturnsWorkoutPlanWithAlignmentZero() {
    PersonSimple stored = basePerson("mobile-reco-workout-alignment-zero");
    stored.setPlanStrategy(PlanStrategy.WORKOUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(new HealthInsightResult(
            INSIGHT_BMI_VALUE,
            INSIGHT_BMI_CATEGORY,
            INSIGHT_HEALTH_INDEX,
            0.0, // planAlignmentIndex = 0
            INSIGHT_OVERALL_SCORE,
            INSIGHT_PERCENTILE,
            null,
            "Test message"));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String workoutPlan = (String) body.get("workoutPlan");
    assertTrue(workoutPlan.contains("Cannot provide a workout plan") || workoutPlan.contains("Plan Alignment = 0"));
  }

  @Test
  @DisplayName("provideRecommendation requires training frequency to be configured")
  void provideRecommendationRequiresTrainingFrequency() {
    PersonSimple stored = basePerson("mobile-reco-workout-goal-null");
    stored.setGoal(FitnessGoal.CUT);
    stored.setPlanStrategy(PlanStrategy.WORKOUT);
    stored.setTrainingFrequencyPerWeek(null); // Missing training frequency
    stored.setTargetChangeKg(2.0);
    stored.setTargetDurationWeeks(8);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.provideRecommendation());

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("All goal plan fields must be configured"));
  }

  @Test
  @DisplayName("provideRecommendation returns workout plan for CUT")
  void provideRecommendationReturnsWorkoutPlanCut() {
    PersonSimple stored = basePerson("mobile-reco-workout-cut");
    stored.setGoal(FitnessGoal.CUT);
    stored.setPlanStrategy(PlanStrategy.WORKOUT);
    stored.setTrainingFrequencyPerWeek(4);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String workoutPlan = (String) body.get("workoutPlan");
    assertTrue(workoutPlan.contains("strength and cardio") || workoutPlan.contains("fat loss"));
  }

  @Test
  @DisplayName("provideRecommendation returns workout plan for BULK")
  void provideRecommendationReturnsWorkoutPlanBulk() {
    PersonSimple stored = basePerson("mobile-reco-workout-bulk");
    stored.setGoal(FitnessGoal.BULK);
    stored.setPlanStrategy(PlanStrategy.WORKOUT);
    stored.setTrainingFrequencyPerWeek(4);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String workoutPlan = (String) body.get("workoutPlan");
    assertTrue(workoutPlan.contains("strength-focused") || workoutPlan.contains("progressive overload"));
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for CUT - target weight exactly at minimum")
  void configureGoalPlanValidatesTargetWeightCutAtMinimum() {
    PersonSimple stored = basePerson("mobile-target-weight-cut-min");
    stored.setWeight(35.0);
    // Use shorter person: for BMI = 15.0, weight = 15 * (height/100)^2
    // For weight = 30kg, height = sqrt(30/15) * 100 = sqrt(2) * 100 ≈ 141.4cm
    // Use 140cm to ensure BMI >= 15.0
    stored.setHeight(140.0); // Shorter person so 30kg gives BMI >= 15.0
    stored.setGoal(FitnessGoal.CUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(5.0); // Would result in exactly 30kg (at minimum weight)
    // BMI = 30 / (1.4^2) = 30 / 1.96 ≈ 15.3 (>= 15.0, passes BMI check)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for CUT - target BMI exactly at minimum")
  void configureGoalPlanValidatesTargetWeightCutAtMinimumBMI() {
    PersonSimple stored = basePerson("mobile-target-weight-cut-min-bmi");
    stored.setWeight(70.0);
    stored.setHeight(200.0); // Tall person
    stored.setGoal(FitnessGoal.CUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    // For BMI = 15: weight = 15 * (2.0^2) = 15 * 4 = 60kg
    // Current weight = 70kg, so target change = 10kg -> target weight = 60kg, BMI = 60/4 = 15 (exactly at minimum)
    request.setTargetChangeKg(10.0); // Would result in 60kg, BMI = 15.0 (exactly at minimum)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for BULK - target weight exactly at maximum")
  void configureGoalPlanValidatesTargetWeightBulkAtMaximum() {
    PersonSimple stored = basePerson("mobile-target-weight-bulk-max");
    stored.setWeight(195.0);
    stored.setHeight(200.0); // Tall person to avoid BMI issues
    stored.setGoal(FitnessGoal.BULK);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    request.setTargetChangeKg(5.0); // Would result in exactly 200kg (at maximum)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("configureGoalPlan validates target weight for BULK - target BMI exactly at maximum")
  void configureGoalPlanValidatesTargetWeightBulkAtMaximumBMI() {
    PersonSimple stored = basePerson("mobile-target-weight-bulk-max-bmi");
    stored.setWeight(100.0);
    stored.setHeight(150.0); // Shorter person
    stored.setGoal(FitnessGoal.BULK);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(personRepository.save(any(PersonSimple.class))).thenAnswer(invocation -> invocation.getArgument(0));

    GoalPlanRequest request = new GoalPlanRequest();
    // For BMI = 50: weight = 50 * (1.5^2) = 50 * 2.25 = 112.5kg
    // Current weight = 100kg, so target change = 12.5kg to reach BMI = 50
    request.setTargetChangeKg(12.5); // Would result in 112.5kg, BMI = 50.0 (exactly at maximum)
    request.setDurationWeeks(10);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @DisplayName("provideRecommendation returns diet plan for CUT with exactly at max deficit")
  void provideRecommendationReturnsDietPlanCutAtMaxDeficit() {
    PersonSimple stored = basePerson("mobile-reco-cut-max-deficit");
    stored.setGoal(FitnessGoal.CUT);
    // Calculate: targetChangeKg * CALORIES_PER_KG / durationWeeks / DAYS_PER_WEEK = 1500
    // 1500 = targetChangeKg * 7700 / durationWeeks / 7
    // targetChangeKg * 7700 = 1500 * durationWeeks * 7 = 10500 * durationWeeks
    // For durationWeeks = 10: targetChangeKg = 10500 * 10 / 7700 = 13.64kg
    stored.setTargetChangeKg(13.64);
    stored.setTargetDurationWeeks(10);
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String dietPlan = (String) body.get("dietPlan");
    assertTrue(dietPlan.contains("kcal deficit per day"));
  }

  @Test
  @DisplayName("provideRecommendation returns diet plan for BULK with exactly at max surplus")
  void provideRecommendationReturnsDietPlanBulkAtMaxSurplus() {
    PersonSimple stored = basePerson("mobile-reco-bulk-max-surplus");
    stored.setGoal(FitnessGoal.BULK);
    // Calculate: targetChangeKg * CALORIES_PER_KG / durationWeeks / DAYS_PER_WEEK = 1000
    // 1000 = targetChangeKg * 7700 / durationWeeks / 7
    // targetChangeKg * 7700 = 1000 * durationWeeks * 7 = 7000 * durationWeeks
    // For durationWeeks = 10: targetChangeKg = 7000 * 10 / 7700 = 9.09kg
    stored.setTargetChangeKg(9.09);
    stored.setTargetDurationWeeks(10);
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    String dietPlan = (String) body.get("dietPlan");
    assertTrue(dietPlan.contains("kcal surplus daily"));
  }

  @Test
  @DisplayName("provideRecommendation returns diet plan with BOTH strategy")
  void provideRecommendationReturnsDietPlanWithBothStrategy() {
    PersonSimple stored = basePerson("mobile-reco-both-strategy");
    stored.setGoal(FitnessGoal.CUT);
    stored.setPlanStrategy(PlanStrategy.BOTH);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));
    when(healthInsightService.buildInsights(stored))
        .thenReturn(sampleInsight("Test message", null));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> body = response.getBody();
    assertNotNull(body.get("dietPlan"));
    assertNotNull(body.get("workoutPlan"));
  }

  @Test
  @DisplayName("provideRecommendation requires plan strategy to be configured")
  void provideRecommendationRequiresPlanStrategy() {
    PersonSimple stored = basePerson("mobile-reco-null-strategy");
    stored.setPlanStrategy(null); // Missing plan strategy
    stored.setTargetChangeKg(2.0);
    stored.setTargetDurationWeeks(8);
    stored.setTrainingFrequencyPerWeek(4);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> personController.provideRecommendation());

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("All goal plan fields must be configured"));
  }

  private PersonSimple basePerson(String clientId) {
    PersonSimple person = new PersonSimple();
    person.setName("Tester");
    person.setClientId(clientId);
    person.setGoal(FitnessGoal.CUT);
    person.setGender(Gender.MALE);
    person.setBirthDate(DOB_1990_JAN);
    person.setWeight(WEIGHT_BASE_KG);
    person.setHeight(HEIGHT_TALL_CM);
    person.setTargetChangeKg(TARGET_CHANGE_PLAN_KG);
    person.setTargetDurationWeeks(TARGET_DURATION_DEFAULT);
    person.setTrainingFrequencyPerWeek(TRAINING_FREQ_FOUR);
    person.setPlanStrategy(PlanStrategy.DIET);
    return person;
  }
}
