package com.teamx.fitness.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.teamx.fitness.controller.ResearchController;
import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/** Tests the research controller using mocked persistence. */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResearchController")
class ResearchControllerTest {

  @Mock private PersonRepository personRepository;

  private ResearchController controller;

  @BeforeEach
  void setup() {
    controller = new ResearchController(personRepository, new PersonService());
  }

  @AfterEach
  void clearContext() {
    ClientContext.clear();
  }

  @Test
  @DisplayName("demographics throws 403 for mobile clients")
  void demographicsBlocksMobile() {
    ClientContext.setClientId("mobile-app1");
    assertThrows(ResponseStatusException.class, () -> controller.demographics());
  }

  @Test
  @DisplayName("demographics throws when not enough data")
  void demographicsInsufficientData() {
    ClientContext.setClientId("research-tool1");
    when(personRepository.findAll()).thenReturn(List.of());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.demographics());

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  @DisplayName("population health splits metrics across goals")
  void populationHealthAggregates() {
    ClientContext.setClientId("research-tool2");
    when(personRepository.findAll()).thenReturn(samplePeople());

    ResponseEntity<Map<String, Object>> response = controller.populationHealth();

    assertEquals(200, response.getStatusCode().value());
    Map<String, Object> goalSegments = (Map<String, Object>) response.getBody().get("goalSegments");
    Map<String, Object> cut = (Map<String, Object>) goalSegments.get("CUT");
    Map<String, Object> bulk = (Map<String, Object>) goalSegments.get("BULK");
    assertEquals(2, cut.get("count"));
    assertEquals(2, bulk.get("count"));
  }

  @Test
  @DisplayName("population health requires both goals")
  void populationHealthNeedsBothGoals() {
    ClientContext.setClientId("research-tool3");
    when(personRepository.findAll()).thenReturn(List.of(buildPerson(FitnessGoal.CUT)));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.populationHealth());

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  private List<PersonSimple> samplePeople() {
    return List.of(
        buildPerson("P1", FitnessGoal.CUT, Gender.FEMALE, 60.0, 165.0, LocalDate.of(1995, 1, 1)),
        buildPerson("P2", FitnessGoal.CUT, Gender.MALE, 72.0, 178.0, LocalDate.of(1990, 6, 15)),
        buildPerson("P3", FitnessGoal.BULK, Gender.MALE, 80.0, 182.0, LocalDate.of(1988, 3, 20)),
        buildPerson("P4", FitnessGoal.BULK, Gender.FEMALE, 68.0, 170.0, LocalDate.of(1992, 9, 5)));
  }

  private PersonSimple buildPerson(FitnessGoal goal) {
    return buildPerson("Test", goal, Gender.MALE, 70.0, 175.0, LocalDate.of(1990, 1, 1));
  }

  private PersonSimple buildPerson(
      String name, FitnessGoal goal, Gender gender, Double weight, Double height, LocalDate birthDate) {
    PersonSimple person = new PersonSimple();
    person.setName(name);
    person.setGoal(goal);
    person.setGender(gender);
    person.setWeight(weight);
    person.setHeight(height);
    person.setBirthDate(birthDate);
    person.setClientId("mobile-" + name);
    return person;
  }
}
