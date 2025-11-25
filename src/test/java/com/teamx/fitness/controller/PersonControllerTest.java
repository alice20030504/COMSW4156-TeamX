package com.teamx.fitness.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.teamx.fitness.model.PlanStrategy;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
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

  @Mock private PersonService personService;

  @Mock private PersonRepository personRepository;

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
            70.0,
            175.0,
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            FitnessGoal.CUT,
            "mobile-normal");
    PersonSimple underweight =
        new PersonSimple(
            "Under",
            50.0,
            180.0,
            LocalDate.of(1992, 2, 2),
            Gender.FEMALE,
            FitnessGoal.BULK,
            "mobile-under");
    PersonSimple overweight =
        new PersonSimple(
            "Over",
            85.0,
            178.0,
            LocalDate.of(1988, 3, 3),
            Gender.MALE,
            FitnessGoal.CUT,
            "mobile-over");
    PersonSimple obese =
        new PersonSimple(
            "Obese",
            110.0,
            170.0,
            LocalDate.of(1985, 4, 4),
            Gender.FEMALE,
            FitnessGoal.BULK,
            "mobile-obese");
    return Stream.of(
        Arguments.of("Normal category", normal, 22.86, "Normal weight"),
        Arguments.of("Underweight category", underweight, 15.43, "Underweight"),
        Arguments.of("Overweight category", overweight, 26.82, "Overweight"),
        Arguments.of("Obese category", obese, 38.06, "Obese"),
        Arguments.of("Unknown BMI when service returns null", normal, null, "Unknown"));
  }

  @Test
  @DisplayName("calculateBMI throws 404 when profile not found")
  void calculateBMIThrowsWhenProfileMissing() {
    ClientContext.setClientId("mobile-missing");
    when(personRepository.findByClientId("mobile-missing")).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> personController.calculateBMI());
  }
  
  private static Stream<Arguments> calorieScenarios() {
    return Stream.of(
        Arguments.of(
            "Male moderate activity",
            70.0,
            175.0,
            30,
            "male",
            3,
            1680.0,
            2604.0),
        Arguments.of(
            "Sedentary frequency",
            70.0,
            175.0,
            30,
            "male",
            0,
            1680.0,
            2016.0),
        Arguments.of(
            "Uppercase gender handled",
            70.0,
            175.0,
            30,
            "MALE",
            4,
            1680.0,
            2604.0),
        Arguments.of(
            "Missing BMR prevents calorie calculation",
            70.0,
            175.0,
            30,
            "female",
            2,
            null,
            null));
  }

  @Test
  @DisplayName("createPerson rejects birthDate equal to today")
  void createPersonRejectsBirthDateToday() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("Test");
    request.setWeight(70.0);
    request.setHeight(170.0);
    request.setBirthDate(LocalDate.now());
    request.setGoal(FitnessGoal.CUT);
    request.setGender(Gender.MALE);

    when(personService.calculateBMI(70.0, 170.0)).thenReturn(24.22);

    assertThrows(ResponseStatusException.class, () -> personController.createPerson(request));
  }

  @Test
  @DisplayName("createPerson rejects invalid weight")
  void createPersonRejectsInvalidWeight() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("Test2");
    request.setWeight(-10.0);
    request.setHeight(170.0);
    request.setBirthDate(LocalDate.of(1990, 1, 1));
    request.setGoal(FitnessGoal.BULK);
    request.setGender(Gender.FEMALE);

    when(personService.calculateBMI(-10.0, 170.0))
        .thenThrow(
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "weight must be greater than 0"));

    assertThrows(ResponseStatusException.class, () -> personController.createPerson(request));
  }

  @Test
  @DisplayName("createPerson returns generated client identifier")
  void createPersonReturnsClientId() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("Valid");
    request.setWeight(70.0);
    request.setHeight(170.0);
    request.setBirthDate(LocalDate.of(1990, 1, 1));
    request.setGoal(FitnessGoal.CUT);
    request.setGender(Gender.MALE);

    when(personService.calculateBMI(70.0, 170.0)).thenReturn(24.22);
    lenient().when(personRepository.findByClientId(anyString())).thenReturn(Optional.empty());
    when(personRepository.save(any(PersonSimple.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, PersonSimple.class));

    ResponseEntity<PersonCreatedResponse> response = personController.createPerson(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    PersonCreatedResponse body = response.getBody();
    assertNotNull(body);
    assertNotNull(body.getClientId());
    assertEquals(true, body.getClientId().startsWith(ClientContext.MOBILE_PREFIX));
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
    request.setTargetChangeKg(2.5);
    request.setDurationWeeks(6);
    request.setTrainingFrequencyPerWeek(4);
    request.setPlanStrategy(PlanStrategy.BOTH);

    ResponseEntity<PersonProfileResponse> response = personController.configureGoalPlan(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ArgumentCaptor<PersonSimple> captor = ArgumentCaptor.forClass(PersonSimple.class);
    verify(personRepository).save(captor.capture());
    PersonSimple saved = captor.getValue();
    assertEquals(2.5, saved.getTargetChangeKg());
    assertEquals(6, saved.getTargetDurationWeeks());
    assertEquals(4, saved.getTrainingFrequencyPerWeek());
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
    request.setTargetChangeKg(2.0);
    request.setDurationWeeks(null);
    request.setTrainingFrequencyPerWeek(3);
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
    when(personService.calculateBMI(anyDouble(), anyDouble())).thenReturn(24.0);

    PersonSimple update = new PersonSimple();
    update.setName("  Updated User  ");
    update.setWeight(82.0);
    update.setHeight(181.0);
    update.setBirthDate(LocalDate.of(1990, 5, 5));
    update.setGender(Gender.MALE);
    update.setGoal(FitnessGoal.BULK);
    update.setTargetChangeKg(3.0);
    update.setTargetDurationWeeks(10);
    update.setTrainingFrequencyPerWeek(5);
    update.setPlanStrategy(PlanStrategy.WORKOUT);

    ResponseEntity<PersonSimple> response = personController.updatePerson(update);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    PersonSimple saved = response.getBody();
    assertNotNull(saved);
    assertEquals("Updated User", saved.getName());
    assertEquals(82.0, saved.getWeight());
    assertEquals(PlanStrategy.WORKOUT, saved.getPlanStrategy());
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
    when(personService.calculateAge(stored.getBirthDate())).thenReturn(30);
    when(personService.calculateBMR(stored.getWeight(), stored.getHeight(), 30, true))
        .thenReturn(1600.0);
    when(personService.calculateDailyCalorieNeeds(1600.0, stored.getTrainingFrequencyPerWeek()))
        .thenReturn(2500.0);

    ResponseEntity<Map<String, Object>> response = personController.calculateDailyCalories();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> body = response.getBody();
    assertEquals(FitnessGoal.CUT, body.get("goal"));
    assertEquals(2500.0, body.get("maintenanceCalories"));
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
    stored.setTargetDurationWeeks(0);
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

    when(personService.calculateAge(stored.getBirthDate())).thenReturn(30);
    when(personService.calculateBMR(stored.getWeight(), stored.getHeight(), 30, true))
        .thenReturn(null);
    assertThrows(ResponseStatusException.class, () -> personController.calculateDailyCalories());

    when(personService.calculateBMR(stored.getWeight(), stored.getHeight(), 30, true))
        .thenReturn(1500.0);
    when(personService.calculateDailyCalorieNeeds(1500.0, stored.getTrainingFrequencyPerWeek()))
        .thenReturn(null);
    assertThrows(ResponseStatusException.class, () -> personController.calculateDailyCalories());
  }

  @Test
  @DisplayName("provideRecommendation composes strategy notes")
  void provideRecommendationIncludesStrategy() {
    PersonSimple stored = basePerson("mobile-reco");
    stored.setGoal(FitnessGoal.BULK);
    stored.setPlanStrategy(PlanStrategy.WORKOUT);
    stored.setTargetChangeKg(3.0);
    stored.setTargetDurationWeeks(8);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    String message = (String) response.getBody().get("message");
    assertNotNull(message);
    org.junit.jupiter.api.Assertions.assertTrue(message.contains("bulking"));
  }

  @Test
  @DisplayName("provideRecommendation handles CUT goals without targets")
  void provideRecommendationHandlesCutWithoutTargets() {
    PersonSimple stored = basePerson("mobile-reco-cut");
    stored.setGoal(FitnessGoal.CUT);
    stored.setPlanStrategy(PlanStrategy.DIET);
    stored.setTargetChangeKg(null);
    stored.setTargetDurationWeeks(null);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    String message = (String) response.getBody().get("message");
    assertNotNull(message);
    org.junit.jupiter.api.Assertions.assertTrue(message.contains("cutting"));
    Map<String, Object> details = response.getBody();
    org.junit.jupiter.api.Assertions.assertTrue(details.containsKey("dietPlan"));
  }

  @Test
  @DisplayName("provideRecommendation handles missing goal and BOTH strategy")
  void provideRecommendationHandlesUnknownGoal() {
    PersonSimple stored = basePerson("mobile-reco-unknown");
    stored.setGoal(null);
    stored.setPlanStrategy(PlanStrategy.BOTH);
    stored.setTrainingFrequencyPerWeek(0);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    Map<String, Object> details = response.getBody();
    assertNotNull(details);
    org.junit.jupiter.api.Assertions.assertTrue(details.containsKey("dietPlan"));
    org.junit.jupiter.api.Assertions.assertTrue(details.containsKey("workoutPlan"));
  }

  @Test
  @DisplayName("provideRecommendation includes BOTH strategy note")
  void provideRecommendationIncludesBothStrategyMessage() {
    PersonSimple stored = basePerson("mobile-reco-both");
    stored.setGoal(FitnessGoal.BULK);
    stored.setPlanStrategy(PlanStrategy.BOTH);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    String message = (String) response.getBody().get("message");
    assertNotNull(message);
    org.junit.jupiter.api.Assertions.assertTrue(message.contains("Balance training and nutrition"));
  }

  @Test
  @DisplayName("provideRecommendation handles BULK goal without targets")
  void provideRecommendationBulkWithoutTargets() {
    PersonSimple stored = basePerson("mobile-reco-bulk-missing");
    stored.setGoal(FitnessGoal.BULK);
    stored.setTargetChangeKg(null);
    stored.setTargetDurationWeeks(null);
    stored.setPlanStrategy(PlanStrategy.WORKOUT);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    String message = (String) response.getBody().get("message");
    assertNotNull(message);
    org.junit.jupiter.api.Assertions.assertTrue(message.contains("Keep bulking—focus"));
    org.junit.jupiter.api.Assertions.assertTrue(message.contains("structured training sessions"));
  }

  @Test
  @DisplayName("provideRecommendation handles CUT goal with explicit targets")
  void provideRecommendationCutWithTargets() {
    PersonSimple stored = basePerson("mobile-reco-cut-target");
    stored.setGoal(FitnessGoal.CUT);
    stored.setTargetChangeKg(2.0);
    stored.setTargetDurationWeeks(6);
    stored.setPlanStrategy(PlanStrategy.DIET);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    String message = (String) response.getBody().get("message");
    assertNotNull(message);
    org.junit.jupiter.api.Assertions.assertTrue(message.contains("target 2.0 kg over 6 weeks"));
  }

  @Test
  @DisplayName("provideRecommendation omits strategy note when planStrategy is null")
  void provideRecommendationOmitsStrategyWhenPlanMissing() {
    PersonSimple stored = basePerson("mobile-reco-no-strategy");
    stored.setPlanStrategy(null);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    String message = (String) response.getBody().get("message");
    assertNotNull(message);
    org.junit.jupiter.api.Assertions.assertFalse(message.contains("Balance training and nutrition"));
    Map<String, Object> body = response.getBody();
    org.junit.jupiter.api.Assertions.assertFalse(body.containsKey("dietPlan"));
    org.junit.jupiter.api.Assertions.assertFalse(body.containsKey("workoutPlan"));
  }

  @Test
  @DisplayName("provideRecommendation supplies workout plan for CUT goal")
  void provideRecommendationGeneratesWorkoutPlanForCutGoal() {
    PersonSimple stored = basePerson("mobile-reco-cut-workout");
    stored.setGoal(FitnessGoal.CUT);
    stored.setPlanStrategy(PlanStrategy.WORKOUT);
    stored.setTrainingFrequencyPerWeek(5);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    String workoutPlan = (String) response.getBody().get("workoutPlan");
    assertNotNull(workoutPlan);
    org.junit.jupiter.api.Assertions.assertTrue(workoutPlan.contains("mixing strength and cardio"));
  }

  @Test
  @DisplayName("provideRecommendation supplies workout plan when goal missing")
  void provideRecommendationGeneratesWorkoutPlanWhenGoalMissing() {
    PersonSimple stored = basePerson("mobile-reco-no-goal-workout");
    stored.setGoal(null);
    stored.setPlanStrategy(PlanStrategy.WORKOUT);
    stored.setTrainingFrequencyPerWeek(0);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    String workoutPlan = (String) response.getBody().get("workoutPlan");
    assertNotNull(workoutPlan);
    org.junit.jupiter.api.Assertions.assertTrue(workoutPlan.contains("total-body"));
  }

  @Test
  @DisplayName("provideRecommendation supplies diet plan when goal missing")
  void provideRecommendationGeneratesDietPlanWhenGoalMissing() {
    PersonSimple stored = basePerson("mobile-reco-no-goal-diet");
    stored.setGoal(null);
    stored.setPlanStrategy(PlanStrategy.DIET);
    stored.setTargetChangeKg(null);
    stored.setTargetDurationWeeks(null);
    ClientContext.setClientId(stored.getClientId());
    when(personRepository.findByClientId(stored.getClientId())).thenReturn(Optional.of(stored));

    ResponseEntity<Map<String, Object>> response = personController.provideRecommendation();

    String dietPlan = (String) response.getBody().get("dietPlan");
    assertNotNull(dietPlan);
    org.junit.jupiter.api.Assertions.assertTrue(dietPlan.contains("balanced meal plan"));
  }

  @Test
  @DisplayName("createPerson retries client ID generation when collisions occur")
  void createPersonRetriesClientIdGeneration() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("Retry");
    request.setWeight(70.0);
    request.setHeight(170.0);
    request.setBirthDate(LocalDate.of(1990, 1, 1));
    request.setGoal(FitnessGoal.CUT);
    request.setGender(Gender.MALE);

    when(personService.calculateBMI(70.0, 170.0)).thenReturn(24.22);
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

  private PersonSimple basePerson(String clientId) {
    PersonSimple person = new PersonSimple();
    person.setName("Tester");
    person.setClientId(clientId);
    person.setGoal(FitnessGoal.CUT);
    person.setGender(Gender.MALE);
    person.setBirthDate(LocalDate.of(1990, 1, 1));
    person.setWeight(80.0);
    person.setHeight(180.0);
    person.setTargetChangeKg(2.0);
    person.setTargetDurationWeeks(8);
    person.setTrainingFrequencyPerWeek(4);
    person.setPlanStrategy(PlanStrategy.DIET);
    return person;
  }
}
