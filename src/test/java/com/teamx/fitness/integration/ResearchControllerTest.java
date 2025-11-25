package com.teamx.fitness.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.teamx.fitness.controller.dto.ResearcherCreateRequest;
import com.teamx.fitness.controller.dto.ResearcherCreatedResponse;
import com.teamx.fitness.controller.ResearchController;
import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.model.Researcher;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.repository.ResearcherRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  
  @Mock private ResearcherRepository researcherRepository;

  private ResearchController controller;

  @BeforeEach
  void setup() {
    controller = new ResearchController(personRepository, new PersonService(), researcherRepository);
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

  @Test
  @DisplayName("registerResearcher creates unique client id")
  void registerResearcherCreatesClientId() {
    ResearcherCreateRequest request = new ResearcherCreateRequest();
    request.setName("Analyst");
    request.setEmail("analyst@example.com");

    when(researcherRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(researcherRepository.findByClientId(any(String.class))).thenReturn(Optional.empty());
    when(researcherRepository.save(any(Researcher.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Researcher.class));

    ResponseEntity<ResearcherCreatedResponse> response = controller.registerResearcher(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(true, response.getBody().getClientId().startsWith(ClientContext.RESEARCH_PREFIX));
  }

  @Test
  @DisplayName("registerResearcher rejects duplicate email")
  void registerResearcherRejectsDuplicateEmail() {
    ResearcherCreateRequest request = new ResearcherCreateRequest();
    request.setName("Duplicate");
    request.setEmail("dup@example.com");
    when(researcherRepository.existsByEmail("dup@example.com")).thenReturn(true);

    assertThrows(ResponseStatusException.class, () -> controller.registerResearcher(request));
  }

  @Test
  @DisplayName("registerResearcher retries client id generation on collision")
  void registerResearcherRetriesIdGeneration() {
    ResearcherCreateRequest request = new ResearcherCreateRequest();
    request.setName("Retry");
    request.setEmail("retry@example.com");

    when(researcherRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(researcherRepository.findByClientId("research-id1"))
        .thenReturn(Optional.of(new Researcher()));
    when(researcherRepository.findByClientId("research-id2"))
        .thenReturn(Optional.empty());
    when(researcherRepository.save(any(Researcher.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Researcher.class));

    ResponseEntity<ResearcherCreatedResponse> response = controller.registerResearcher(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("research-id2", response.getBody().getClientId());
  }

  @Test
  @DisplayName("demographics enforces minimum sample size")
  void demographicsRequiresMinimumSample() {
    ClientContext.setClientId("research-tool4");
    when(personRepository.findAll()).thenReturn(List.of(buildPerson(FitnessGoal.CUT)));

    assertThrows(ResponseStatusException.class, () -> controller.demographics());
  }

  @Test
  @DisplayName("demographics returns summary when data available")
  void demographicsReturnsSummary() {
    ClientContext.setClientId("research-tool5");
    when(personRepository.findAll()).thenReturn(samplePeople());

    ResponseEntity<Map<String, Object>> response = controller.demographics();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> summary = (Map<String, Object>) response.getBody().get("cohortSummary");
    assertEquals(4, summary.get("sampleSize"));
  }

  @Test
  @DisplayName("demographics requires complete metrics")
  void demographicsRequiresCompleteMetrics() {
    ClientContext.setClientId("research-tool6");
    List<PersonSimple> incomplete = new java.util.ArrayList<>(samplePeople());
    incomplete.forEach(person -> person.setWeight(null));
    when(personRepository.findAll()).thenReturn(incomplete);

    assertThrows(ResponseStatusException.class, () -> controller.demographics());
  }

  @Test
  @DisplayName("goalMetrics requires complete weight and bmi data")
  void goalMetricsRequiresCompleteData() {
    ClientContext.setClientId("research-tool7");
    PersonSimple person = buildPerson(FitnessGoal.CUT);
    person.setWeight(null);
    when(personRepository.findAll()).thenReturn(List.of(person, person, person));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.populationHealth());

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  @DisplayName("goalMetrics requires height data for BMI computation")
  void goalMetricsRequiresHeightData() {
    ClientContext.setClientId("research-tool8");
    PersonSimple cut = buildPerson("CutA", FitnessGoal.CUT, Gender.MALE, 70.0, null, LocalDate.of(1990, 1, 1));
    PersonSimple cut2 = buildPerson("CutB", FitnessGoal.CUT, Gender.FEMALE, 65.0, null, LocalDate.of(1992, 5, 5));
    PersonSimple bulk = buildPerson("BulkA", FitnessGoal.BULK, Gender.MALE, 80.0, 180.0, LocalDate.of(1988, 3, 3));
    PersonSimple bulk2 = buildPerson("BulkB", FitnessGoal.BULK, Gender.FEMALE, 75.0, 170.0, LocalDate.of(1989, 7, 7));
    when(personRepository.findAll()).thenReturn(List.of(cut, cut2, bulk, bulk2));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.populationHealth());

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  @DisplayName("demographics requires birth dates for age computation")
  void demographicsRequiresBirthDates() {
    ClientContext.setClientId("research-tool9");
    List<PersonSimple> incomplete = new java.util.ArrayList<>(samplePeople());
    incomplete.forEach(person -> person.setBirthDate(null));
    when(personRepository.findAll()).thenReturn(incomplete);

    assertThrows(ResponseStatusException.class, () -> controller.demographics());
  }

  @Test
  @DisplayName("generateResearchClientId throws when no identifiers available")
  void generateResearchClientIdThrowsWhenExhausted() {
    ResearcherCreateRequest request = new ResearcherCreateRequest();
    request.setName("Overflow");
    request.setEmail("overflow@example.com");
    when(researcherRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(researcherRepository.findByClientId(any(String.class))).thenReturn(Optional.of(new Researcher()));

    assertThrows(ResponseStatusException.class, () -> controller.registerResearcher(request));
  }

  @Test
  @DisplayName("round returns NaN for invalid numbers")
  void roundHandlesInvalidNumbers() throws Exception {
    java.lang.reflect.Method method =
        ResearchController.class.getDeclaredMethod("round", double.class);
    method.setAccessible(true);

    double nanResult = (double) method.invoke(controller, Double.NaN);
    double infResult = (double) method.invoke(controller, Double.POSITIVE_INFINITY);

    org.junit.jupiter.api.Assertions.assertTrue(Double.isNaN(nanResult));
    org.junit.jupiter.api.Assertions.assertTrue(Double.isNaN(infResult));
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
