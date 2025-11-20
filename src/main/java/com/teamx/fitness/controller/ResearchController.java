package com.teamx.fitness.controller;

import com.teamx.fitness.controller.dto.ResearcherCreateRequest;
import com.teamx.fitness.controller.dto.ResearcherCreatedResponse;
import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.model.Researcher;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.repository.ResearcherRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Research Controller - Aggregated views backed by persisted person profiles.
 */
@RestController
@RequestMapping("/api/research")
@CrossOrigin(origins = "*")
@Tag(
    name = "Research Controller",
    description = "Aggregated, anonymized insights derived from persisted person profiles."
        + " Research clients only.")
public class ResearchController {

  /** Repository for person data persistence. */
  private final PersonRepository personRepository;
  
  /** Service for handling person-related business logic. */
  private final PersonService personService;
  
  /** Repository for researcher data persistence. */
  private final ResearcherRepository researcherRepository;

  public ResearchController(
      PersonRepository personRepository,
      PersonService personService,
      ResearcherRepository researcherRepository) {
    this.personRepository = personRepository;
    this.personService = personService;
    this.researcherRepository = researcherRepository;
  }

  /** Minimum sample size required for research metrics. */
  private static final int MIN_SAMPLE_SIZE = 3;

  @PostMapping
  @Operation(
      summary = "Register a new researcher profile",
      description = "Registers a new researcher with name and email. "
          + "Returns the generated client identifier to use for subsequent requests."
          + " This is the only research endpoint that does not require the `X-Client-ID` header.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Researcher created successfully",
          content = @Content(
              schema = @Schema(implementation = ResearcherCreatedResponse.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "clientId": "research-3f2a4b1cd8e94bceb8c0b6a7dd5f1e92"
                      }
                      """))),
      @ApiResponse(responseCode = "400", description = "Invalid input data or email already exists")
  })
  public ResponseEntity<ResearcherCreatedResponse> registerResearcher(
      @Valid @RequestBody ResearcherCreateRequest request) {

    if (researcherRepository.existsByEmail(request.getEmail())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Email already registered");
    }

    Researcher researcher = new Researcher();
    researcher.setName(request.getName().trim());
    researcher.setEmail(request.getEmail().trim().toLowerCase());
    researcher.setClientId(generateResearchClientId());

    Researcher saved = researcherRepository.save(researcher);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ResearcherCreatedResponse(saved.getClientId()));
  }

  private String generateResearchClientId() {
    final int maxAttempts = 1000;
    for (int suffix = 1; suffix <= maxAttempts; suffix++) {
      String candidate = ClientContext.RESEARCH_PREFIX + "id" + suffix;
      if (researcherRepository.findByClientId(candidate).isEmpty()) {
        return candidate;
      }
    }
    throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR, "Unable to generate a unique client identifier");
  }

  private void validateResearchAccess() {
    String clientId = ClientContext.getClientId();
    if (ClientContext.isMobileClient(clientId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Mobile clients are not authorized to access research endpoints."
              + " Research endpoints are restricted to research clients only.");
    }
  }

  private List<PersonSimple> loadPeople() {
    List<PersonSimple> persons = personRepository.findAll();
    if (persons.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Not enough data to compute research metrics yet."
              + " Create some person profiles first.");
    }
    return persons;
  }

  private double safeAverage(List<Double> values) {
    return values.stream()
        .filter(Objects::nonNull)
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(Double.NaN);
  }

  private double safeAverageInt(List<Integer> values) {
    return values.stream()
        .filter(Objects::nonNull)
        .mapToInt(Integer::intValue)
        .average()
        .orElse(Double.NaN);
  }

  private double round(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return Double.NaN;
    }
    return Math.round(value * 100.0) / 100.0;
  }

  @GetMapping("/demographics")
  @Operation(
      summary = "Aggregated demographic snapshot",
      description = "Returns sample size, averages, and distribution by gender and goal.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Research client identifier",
              example = "research-tool1")
      })
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Demographic snapshot computed",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
                  {
                    "cohortSummary": {
                      "sampleSize": 4,
                      "averageAge": 32.5,
                      "averageWeight": 72.5,
                      "averageHeight": 175.0
                    },
                    "breakdown": {
                      "byGender": {"MALE": 2, "FEMALE": 2},
                      "byGoal": {"CUT": 2, "BULK": 2}
                    }
                  }
                  """))),
      @ApiResponse(responseCode = "400", description = "Not enough data to compute metrics"),
      @ApiResponse(responseCode = "403", description = "Forbidden for mobile clients")
  })
  public ResponseEntity<Map<String, Object>> demographics() {
    validateResearchAccess();
    List<PersonSimple> persons = loadPeople();

    if (persons.size() < MIN_SAMPLE_SIZE) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Not enough data to compute metrics yet."
              + " Create at least " + MIN_SAMPLE_SIZE + " profiles.");
    }

    List<Double> weights = persons.stream()
        .map(PersonSimple::getWeight)
        .filter(Objects::nonNull)
        .toList();
    List<Double> heights = persons.stream()
        .map(PersonSimple::getHeight)
        .filter(Objects::nonNull)
        .toList();
    List<Integer> ages = persons.stream()
        .map(PersonSimple::getBirthDate)
        .filter(Objects::nonNull)
        .map(personService::calculateAge)
        .filter(Objects::nonNull)
        .toList();

    if (weights.isEmpty() || heights.isEmpty() || ages.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Not enough complete data to compute demographics."
              + " Ensure weight, height, and birthDate are provided.");
    }

    Map<String, Object> summary = new HashMap<>();
    summary.put("sampleSize", persons.size());
    summary.put("averageAge", round(safeAverageInt(ages)));
    summary.put("averageWeight", round(safeAverage(weights)));
    summary.put("averageHeight", round(safeAverage(heights)));

    Map<String, Long> byGender = persons.stream()
        .filter(p -> p.getGender() != null)
        .collect(Collectors.groupingBy(p -> p.getGender().name(), Collectors.counting()));

    Map<String, Long> byGoal = persons.stream()
        .filter(p -> p.getGoal() != null)
        .collect(Collectors.groupingBy(p -> p.getGoal().name(), Collectors.counting()));

    Map<String, Object> breakdown = new HashMap<>();
    breakdown.put("byGender", byGender);
    breakdown.put("byGoal", byGoal);

    Map<String, Object> response = new HashMap<>();
    response.put("cohortSummary", summary);
    response.put("breakdown", breakdown);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/population-health")
  @Operation(
      summary = "Population health overview",
      description = "Basic averages split by goal along with BMI insights.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Research client identifier",
              example = "research-tool1")
      })
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Population health metrics computed",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
                  {
                    "totalProfiles": 4,
                    "goalSegments": {
                      "CUT": {
                        "count": 2,
                        "averageWeight": 68.0,
                        "averageBMI": 23.5
                      },
                      "BULK": {
                        "count": 2,
                        "averageWeight": 77.0,
                        "averageBMI": 24.8
                      }
                    }
                  }
                  """))),
      @ApiResponse(responseCode = "400", description = "Not enough data to compute metrics"),
      @ApiResponse(responseCode = "403", description = "Forbidden for mobile clients")
  })
  public ResponseEntity<Map<String, Object>> populationHealth() {
    validateResearchAccess();
    List<PersonSimple> persons = loadPeople();

    Map<FitnessGoal, List<PersonSimple>> byGoal = persons.stream()
        .filter(p -> p.getGoal() != null)
        .collect(Collectors.groupingBy(PersonSimple::getGoal));

    List<PersonSimple> cutters = byGoal.get(FitnessGoal.CUT);
    List<PersonSimple> bulkers = byGoal.get(FitnessGoal.BULK);

    if (cutters == null || cutters.isEmpty() || bulkers == null || bulkers.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Not enough CUT and BULK data to produce population health metrics."
              + " Create more person profiles with both goals.");
    }

    Map<String, Object> response = new HashMap<>();
    response.put("totalProfiles", persons.size());

    Map<String, Object> goalSegments = new HashMap<>();
    goalSegments.put("CUT", goalMetrics(cutters));
    goalSegments.put("BULK", goalMetrics(bulkers));

    response.put("goalSegments", goalSegments);
    return ResponseEntity.ok(response);
  }

  private Map<String, Object> goalMetrics(List<PersonSimple> people) {
    DoubleSummaryStatistics weightStats = people.stream()
        .map(PersonSimple::getWeight)
        .filter(Objects::nonNull)
        .mapToDouble(Double::doubleValue)
        .summaryStatistics();

    DoubleSummaryStatistics bmiStats = people.stream()
        .filter(p -> p.getWeight() != null && p.getHeight() != null)
        .map(p -> personService.calculateBMI(p.getWeight(), p.getHeight()))
        .filter(Objects::nonNull)
        .mapToDouble(Double::doubleValue)
        .summaryStatistics();

    if (weightStats.getCount() == 0 || bmiStats.getCount() == 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Not enough complete data to compute goal metrics.");
    }

    Map<String, Object> metrics = new HashMap<>();
    metrics.put("count", people.size());
    metrics.put("averageWeight", round(weightStats.getAverage()));
    metrics.put("averageBMI", round(bmiStats.getAverage()));

    Map<String, Long> genderSplit = people.stream()
        .filter(p -> p.getGender() != null)
        .collect(Collectors.groupingBy(p -> p.getGender().name(), Collectors.counting()));
    metrics.put("genderSplit", genderSplit);

    return metrics;
  }
}
