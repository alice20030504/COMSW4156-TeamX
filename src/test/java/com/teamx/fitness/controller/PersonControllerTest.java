package com.teamx.fitness.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.teamx.fitness.model.PersonSimple;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Focused unit tests for {@link PersonController} covering core calculation and client-scoped
 * behaviors with mocked collaborators.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PersonController basic calculations")
class PersonControllerTest {

  @Mock private PersonService personService;

  @Mock private PersonRepository personRepository;

  @InjectMocks private PersonController personController;

  @AfterEach
  void clearClientContext() {
    ClientContext.clear();
  }

  /**
   * Ensures the BMI endpoint returns consistent payloads across representative inputs.
   */
  @ParameterizedTest
  @MethodSource("calculateBmiScenarios")
  @DisplayName("calculateBMI handles valid, boundary, and invalid scenarios")
  void calculateBMIHandlesScenarios(
      String description,
      Double weight,
      Double height,
      Double serviceResult,
      String expectedCategory) {

    when(personService.calculateBMI(weight, height)).thenReturn(serviceResult);

    ResponseEntity<Map<String, Object>> response = personController.calculateBMI(weight, height);

    assertEquals(HttpStatus.OK, response.getStatusCode(), description);
    Map<String, Object> body = response.getBody();
    assertNotNull(body, description);
    assertEquals(weight, body.get("weight"), description);
    assertEquals(height, body.get("height"), description);
    assertEquals(serviceResult, body.get("bmi"), description);
    assertEquals(expectedCategory, body.get("category"), description);
  }

  private static Stream<Arguments> calculateBmiScenarios() {
    return Stream.of(
        Arguments.of("Valid: normal BMI", 70.0, 175.0, 22.86, "Normal weight"),
        Arguments.of("Boundary: underweight classification", 50.0, 180.0, 15.43, "Underweight"),
        Arguments.of("Boundary: overweight classification", 85.0, 178.0, 26.82, "Overweight"),
        Arguments.of("Boundary: obese classification", 110.0, 170.0, 38.06, "Obese"),
        Arguments.of("Invalid: service returns null BMI", 70.0, 0.0, null, "Unknown"));
  }

  /**
   * Confirms the calorie endpoint composes BMR and activity factors while honoring null guards.
   */
  @ParameterizedTest
  @MethodSource("calculateDailyCalorieNeedsScenarios")
  @DisplayName("calculateDailyCalorieNeeds composes BMR and activity factor")
  void calculateDailyCalorieNeedsHandlesScenarios(
      String description,
      Double weight,
      Double height,
      Integer age,
      String gender,
      Integer weeklyTrainingFreq,
      Double bmrResult,
      Double caloriesResult) {

    boolean isMale = "male".equalsIgnoreCase(gender);
    when(personService.calculateBMR(weight, height, age, isMale)).thenReturn(bmrResult);
    when(personService.calculateDailyCalorieNeeds(bmrResult, weeklyTrainingFreq))
        .thenReturn(caloriesResult);

    ResponseEntity<Map<String, Object>> response =
        personController.calculateDailyCalories(weight, height, age, gender, weeklyTrainingFreq);

    assertEquals(HttpStatus.OK, response.getStatusCode(), description);
    Map<String, Object> body = response.getBody();
    assertNotNull(body, description);
    assertEquals(bmrResult, body.get("bmr"), description);
    assertEquals(caloriesResult, body.get("dailyCalories"), description);
    assertEquals(weeklyTrainingFreq, body.get("weeklyTrainingFreq"), description);
  }

  private static Stream<Arguments> calculateDailyCalorieNeedsScenarios() {
    return Stream.of(
        Arguments.of(
            "Valid: male moderate activity",
            70.0,
            175.0,
            30,
            "male",
            3,
            1680.0,
            2604.0),
        Arguments.of(
            "Boundary: sedentary frequency",
            70.0,
            175.0,
            30,
            "male",
            0,
            1680.0,
            2016.0),
        Arguments.of(
            "Boundary: uppercase gender handled as male",
            70.0,
            175.0,
            30,
            "MALE",
            4,
            1680.0,
            2604.0),
        Arguments.of(
            "Invalid: missing BMR prevents calorie calculation",
            70.0,
            175.0,
            30,
            "female",
            2,
            null,
            null));
  }
  
  /**
   * Valid creation scenario demonstrating client ID assignment.
   */
  @Test
  @DisplayName("health reports service availability metadata")
  void healthReturnsMetadata() {
    ResponseEntity<Map<String, Object>> response = personController.health();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> body = response.getBody();
    assertNotNull(body);
    assertEquals("UP", body.get("status"));
    assertEquals("Personal Fitness Management Service", body.get("service"));
    assertEquals("1.0.0", body.get("version"));
  }

  @Test
  @DisplayName("createPerson rejects birthDate equal to today")
  void createPersonRejectsBirthDateToday() {
    PersonSimple p = new PersonSimple();
    p.setName("Test");
    p.setWeight(70.0);
    p.setHeight(170.0);
    p.setBirthDate(LocalDate.now());

    when(personService.calculateBMI(70.0, 170.0)).thenReturn(24.22);

    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> personController.createPerson(p));
  }

  @Test
  @DisplayName("createPerson rejects negative weight")
  void createPersonRejectsNegativeWeight() {
    PersonSimple p = new PersonSimple();
    p.setName("Test2");
    p.setWeight(-10.0);
    p.setHeight(170.0);
    p.setBirthDate(LocalDate.of(1990, 1, 1));

    when(personService.calculateBMI(-10.0, 170.0))
        .thenThrow(new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "weight must be greater than 0"));

    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> personController.createPerson(p));
  }
}