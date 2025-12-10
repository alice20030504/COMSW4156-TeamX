package com.teamx.fitness.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.teamx.fitness.controller.ResearchController;
import com.teamx.fitness.controller.dto.ResearcherCreateRequest;
import com.teamx.fitness.controller.dto.ResearcherCreatedResponse;
import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.model.Researcher;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.repository.ResearcherRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
import java.time.Month;
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

  /** Year constant 1990. */
  private static final int YEAR_1990 = 1990;
  /** Year constant 1992. */
  private static final int YEAR_1992 = 1992;
  /** Year constant 1995. */
  private static final int YEAR_1995 = 1995;
  /** Year constant 1988. */
  private static final int YEAR_1988 = 1988;
  /** Year constant 1989. */
  private static final int YEAR_1989 = 1989;
  /** Month day constant 1. */
  private static final int DAY_ONE = 1;
  /** Month day constant 5. */
  private static final int DAY_FIVE = 5;
  /** Month day constant 7. */
  private static final int DAY_SEVEN = 7;
  /** Month day constant 9. */
  private static final int DAY_NINE = 9;
  /** Month day constant 15. */
  private static final int DAY_FIFTEEN = 15;
  /** Month day constant 20. */
  private static final int DAY_TWENTY = 20;

  /** Default DOB used for sample people. */
  private static final LocalDate DOB_STANDARD =
      LocalDate.of(YEAR_1990, Month.JANUARY, DAY_ONE);
  /** Supplemental female DOB. */
  private static final LocalDate DOB_FEMALE_ALT =
      LocalDate.of(YEAR_1992, Month.MAY, DAY_FIVE);
  /** Supplemental male DOB. */
  private static final LocalDate DOB_MALE_ALT =
      LocalDate.of(YEAR_1995, Month.MARCH, DAY_FIFTEEN);
  /** Additional male DOB used for analytics. */
  private static final LocalDate DOB_MALE_SECOND =
      LocalDate.of(YEAR_1988, Month.MARCH, DAY_SEVEN);
  /** Additional female DOB used for analytics. */
  private static final LocalDate DOB_FEMALE_SECOND =
      LocalDate.of(YEAR_1989, Month.JULY, DAY_SEVEN);
  /** DOB used for supplemental cut female sample. */
  private static final LocalDate DOB_SAMPLE_FEMALE =
      LocalDate.of(YEAR_1995, Month.JANUARY, DAY_ONE);
  /** DOB used for supplemental cut male sample. */
  private static final LocalDate DOB_SAMPLE_MALE =
      LocalDate.of(YEAR_1990, Month.JUNE, DAY_FIFTEEN);
  /** DOB used for bulk male sample. */
  private static final LocalDate DOB_SAMPLE_BULK_MALE =
      LocalDate.of(YEAR_1988, Month.MARCH, DAY_TWENTY);
  /** DOB used for bulk female sample. */
  private static final LocalDate DOB_SAMPLE_BULK_FEMALE =
      LocalDate.of(YEAR_1992, Month.SEPTEMBER, DAY_NINE);

  /** Standard adult weight used for analytics fixtures (kg). */
  private static final double WEIGHT_STANDARD_KG = 70.0;
  /** Alternate female weight (kg). */
  private static final double WEIGHT_FEMALE_KG = 65.0;
  /** Alternate male weight (kg). */
  private static final double WEIGHT_MALE_KG = 80.0;
  /** Supplemental male weight (kg). */
  private static final double WEIGHT_SUPPLEMENTAL_MALE_KG = 75.0;
  /** Cut female sample weight (kg). */
  private static final double WEIGHT_CUT_FEMALE_KG = 60.0;
  /** Cut male sample weight (kg). */
  private static final double WEIGHT_CUT_MALE_KG = 72.0;
  /** Bulk female sample weight (kg). */
  private static final double WEIGHT_BULK_FEMALE_KG = 68.0;
  /** Supplemental male height (cm). */
  private static final double HEIGHT_SUPPLEMENTAL_MALE_CM = 170.0;
  /** Standard adult height used for analytics fixtures (cm). */
  private static final double HEIGHT_STANDARD_CM = 175.0;
  /** Alternate tall height (cm). */
  private static final double HEIGHT_TALL_CM = 180.0;
  /** Supplemental tall height (cm). */
  private static final double HEIGHT_SUPPLEMENTAL_CM = 182.0;
  /** Cut female sample height (cm). */
  private static final double HEIGHT_CUT_FEMALE_CM = 165.0;
  /** Cut male sample height (cm). */
  private static final double HEIGHT_CUT_MALE_CM = 178.0;
  /** Bulk female sample height (cm). */
  private static final double HEIGHT_BULK_FEMALE_CM = 170.0;
  /** Default cohort size expectation. */
  private static final int DEFAULT_SAMPLE_SIZE = 4;
  /** Minimum cohort size required for analytics. */
  private static final int MIN_SAMPLE_SIZE = 4;

  /** Mocked person repository. */
  @Mock private PersonRepository personRepository;
  
  /** Mocked researcher repository. */
  @Mock private ResearcherRepository researcherRepository;

  /** Controller instance under test. */
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

    assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
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
    assertTrue(response.getBody().getClientId().startsWith(ClientContext.RESEARCH_PREFIX));
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
    Map<String, Object> body = response.getBody();
    assertNotNull(body);
    // New structure: sampleSize is at top level, not in cohortSummary
    assertEquals(DEFAULT_SAMPLE_SIZE, body.get("sampleSize"));
    assertNotNull(body.get("ageDistribution"));
    assertNotNull(body.get("genderDistribution"));
    assertNotNull(body.get("physicalCharacteristics"));
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
    PersonSimple cut =
        buildPerson(
            "CutA", FitnessGoal.CUT, Gender.MALE, WEIGHT_STANDARD_KG, null, DOB_STANDARD);
    PersonSimple cut2 =
        buildPerson(
            "CutB", FitnessGoal.CUT, Gender.FEMALE, WEIGHT_FEMALE_KG, null, DOB_FEMALE_ALT);
    PersonSimple bulk =
        buildPerson(
            "BulkA",
            FitnessGoal.BULK,
            Gender.MALE,
            WEIGHT_MALE_KG,
            HEIGHT_TALL_CM,
            DOB_MALE_SECOND);
    PersonSimple bulk2 =
        buildPerson(
            "BulkB",
            FitnessGoal.BULK,
            Gender.FEMALE,
            WEIGHT_SUPPLEMENTAL_MALE_KG,
            HEIGHT_SUPPLEMENTAL_MALE_CM,
            DOB_FEMALE_SECOND);
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
  void roundHandlesInvalidNumbers() {
    double nanResult = controller.round(Double.NaN);
    double infResult = controller.round(Double.POSITIVE_INFINITY);

    assertTrue(Double.isNaN(nanResult));
    assertTrue(Double.isNaN(infResult));
  }

  private List<PersonSimple> samplePeople() {
    return List.of(
        buildPerson(
            "P1",
            FitnessGoal.CUT,
            Gender.FEMALE,
            WEIGHT_CUT_FEMALE_KG,
            HEIGHT_CUT_FEMALE_CM,
            DOB_SAMPLE_FEMALE),
        buildPerson(
            "P2",
            FitnessGoal.CUT,
            Gender.MALE,
            WEIGHT_CUT_MALE_KG,
            HEIGHT_CUT_MALE_CM,
            DOB_SAMPLE_MALE),
        buildPerson(
            "P3",
            FitnessGoal.BULK,
            Gender.MALE,
            WEIGHT_MALE_KG,
            HEIGHT_SUPPLEMENTAL_CM,
            DOB_SAMPLE_BULK_MALE),
        buildPerson(
            "P4",
            FitnessGoal.BULK,
            Gender.FEMALE,
            WEIGHT_BULK_FEMALE_KG,
            HEIGHT_BULK_FEMALE_CM,
            DOB_SAMPLE_BULK_FEMALE));
  }

  private PersonSimple buildPerson(FitnessGoal goal) {
    return buildPerson("Test", goal, Gender.MALE, WEIGHT_STANDARD_KG, HEIGHT_STANDARD_CM, DOB_STANDARD);
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
