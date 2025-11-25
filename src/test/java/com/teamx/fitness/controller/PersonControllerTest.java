package com.teamx.fitness.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.teamx.fitness.controller.dto.PersonCreateRequest;
import com.teamx.fitness.controller.dto.PersonCreatedResponse;
import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
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
}
