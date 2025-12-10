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
import java.util.Locale;
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
              examples = @ExampleObject("""
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
    researcher.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
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

  private double safeAverageInt(List<Integer> values) {
    return values.stream()
        .filter(Objects::nonNull)
        .mapToInt(Integer::intValue)
        .average()
        .orElse(Double.NaN);
  }

  /**
   * Rounds metric values while tolerating NaN input so tests can call directly.
   *
   * @param value number to round
   * @return rounded value or NaN when the input is invalid
   */
  public double round(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return Double.NaN;
    }
    return Math.round(value * 100.0) / 100.0;
  }

  @GetMapping("/demographics")
  @Operation(
      summary = "Demographic characteristics and distributions",
      description = "Returns detailed demographic breakdowns including age ranges, gender distribution, "
          + "and physical characteristics. Focuses on WHO the users are rather than health outcomes.",
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
          description = "Demographic analysis computed",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject("""
                  {
                    "sampleSize": 4,
                    "ageDistribution": {
                      "averageAge": 32.5,
                      "ageRanges": {
                        "18-25": 1,
                        "26-35": 2,
                        "36-45": 1,
                        "46+": 0
                      }
                    },
                    "genderDistribution": {
                      "MALE": 2,
                      "FEMALE": 2,
                      "percentage": {"MALE": 50.0, "FEMALE": 50.0}
                    },
                    "physicalCharacteristics": {
                      "averageWeight": 72.5,
                      "averageHeight": 175.0,
                      "weightRange": {"min": 65.0, "max": 80.0},
                      "heightRange": {"min": 160.0, "max": 190.0}
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

    // Age distribution by ranges
    Map<String, Long> ageRanges = ages.stream()
        .collect(Collectors.groupingBy(age -> {
          if (age < 26) {
            return "18-25";
          }
          if (age < 36) {
            return "26-35";
          }
          if (age < 46) {
            return "36-45";
          }
          return "46+";
        }, Collectors.counting()));

    Map<String, Object> ageDistribution = new HashMap<>();
    ageDistribution.put("averageAge", round(safeAverageInt(ages)));
    ageDistribution.put("ageRanges", ageRanges);

    // Gender distribution with percentages
    Map<String, Long> genderCounts = persons.stream()
        .filter(p -> p.getGender() != null)
        .collect(Collectors.groupingBy(p -> p.getGender().name(), Collectors.counting()));
    
    long totalWithGender = genderCounts.values().stream().mapToLong(Long::longValue).sum();
    Map<String, Double> genderPercentages = new HashMap<>();
    genderCounts.forEach((gender, count) -> {
      genderPercentages.put(gender, round((count * 100.0) / totalWithGender));
    });

    Map<String, Object> genderDistribution = new HashMap<>();
    genderDistribution.putAll(genderCounts);
    genderDistribution.put("percentage", genderPercentages);

    // Physical characteristics with ranges
    DoubleSummaryStatistics weightStats = weights.stream()
        .mapToDouble(Double::doubleValue)
        .summaryStatistics();
    DoubleSummaryStatistics heightStats = heights.stream()
        .mapToDouble(Double::doubleValue)
        .summaryStatistics();

    Map<String, Object> physicalCharacteristics = new HashMap<>();
    physicalCharacteristics.put("averageWeight", round(weightStats.getAverage()));
    physicalCharacteristics.put("averageHeight", round(heightStats.getAverage()));
    Map<String, Double> weightRange = new HashMap<>();
    weightRange.put("min", round(weightStats.getMin()));
    weightRange.put("max", round(weightStats.getMax()));
    physicalCharacteristics.put("weightRange", weightRange);
    Map<String, Double> heightRange = new HashMap<>();
    heightRange.put("min", round(heightStats.getMin()));
    heightRange.put("max", round(heightStats.getMax()));
    physicalCharacteristics.put("heightRange", heightRange);

    Map<String, Object> response = new HashMap<>();
    response.put("sampleSize", persons.size());
    response.put("ageDistribution", ageDistribution);
    response.put("genderDistribution", genderDistribution);
    response.put("physicalCharacteristics", physicalCharacteristics);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/population-health")
  @Operation(
      summary = "Health outcomes and plan effectiveness metrics",
      description = "Returns health metrics, plan configurations, and outcomes grouped by fitness goal. "
          + "Focuses on WHAT users are doing and achieving rather than demographic characteristics.",
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
              examples = @ExampleObject("""
                  {
                    "totalProfiles": 4,
                    "goalSegments": {
                      "CUT": {
                        "count": 2,
                        "healthMetrics": {
                          "averageBMI": 23.5,
                          "bmiDistribution": {
                            "underweight": 0,
                            "normal": 1,
                            "overweight": 1,
                            "obese": 0
                          }
                        },
                        "planMetrics": {
                          "averageTargetChange": 5.0,
                          "averageDurationWeeks": 12.0,
                          "averageTrainingFrequency": 3.5,
                          "planStrategies": {"DIET": 1, "BOTH": 1}
                        }
                      },
                      "BULK": {
                        "count": 2,
                        "healthMetrics": {
                          "averageBMI": 24.8,
                          "bmiDistribution": {
                            "underweight": 0,
                            "normal": 2,
                            "overweight": 0,
                            "obese": 0
                          }
                        },
                        "planMetrics": {
                          "averageTargetChange": 8.0,
                          "averageDurationWeeks": 16.0,
                          "averageTrainingFrequency": 4.0,
                          "planStrategies": {"WORKOUT": 1, "BOTH": 1}
                        }
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
    goalSegments.put("CUT", goalHealthMetrics(cutters));
    goalSegments.put("BULK", goalHealthMetrics(bulkers));

    response.put("goalSegments", goalSegments);
    return ResponseEntity.ok(response);
  }

  private Map<String, Object> goalHealthMetrics(List<PersonSimple> people) {
    // Health metrics (BMI and distribution)
    List<Double> bmis = people.stream()
        .filter(p -> p.getWeight() != null && p.getHeight() != null)
        .map(p -> personService.calculateBMI(p.getWeight(), p.getHeight()))
        .filter(Objects::nonNull)
        .toList();

    if (bmis.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Not enough complete data to compute health metrics.");
    }

    Map<String, Long> bmiDistribution = bmis.stream()
        .collect(Collectors.groupingBy(bmi -> {
          if (bmi < 18.5) {
            return "underweight";
          }
          if (bmi < 25.0) {
            return "normal";
          }
          if (bmi < 30.0) {
            return "overweight";
          }
          return "obese";
        }, Collectors.counting()));

    Map<String, Object> healthMetrics = new HashMap<>();
    healthMetrics.put("averageBMI",
        round(bmis.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN)));
    healthMetrics.put("bmiDistribution", bmiDistribution);

    // Plan metrics (what users are planning to do)
    List<Double> targetChanges = people.stream()
        .map(PersonSimple::getTargetChangeKg)
        .filter(Objects::nonNull)
        .toList();
    List<Integer> durations = people.stream()
        .map(PersonSimple::getTargetDurationWeeks)
        .filter(Objects::nonNull)
        .toList();
    List<Integer> trainingFreqs = people.stream()
        .map(PersonSimple::getTrainingFrequencyPerWeek)
        .filter(Objects::nonNull)
        .toList();
    Map<String, Long> planStrategies = people.stream()
        .filter(p -> p.getPlanStrategy() != null)
        .collect(Collectors.groupingBy(p -> p.getPlanStrategy().name(), Collectors.counting()));

    Map<String, Object> planMetrics = new HashMap<>();
    if (!targetChanges.isEmpty()) {
      planMetrics.put("averageTargetChange",
          round(targetChanges.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN)));
    }
    if (!durations.isEmpty()) {
      planMetrics.put("averageDurationWeeks",
          round(durations.stream().mapToInt(Integer::intValue).average().orElse(Double.NaN)));
    }
    if (!trainingFreqs.isEmpty()) {
      planMetrics.put("averageTrainingFrequency",
          round(trainingFreqs.stream().mapToInt(Integer::intValue).average().orElse(Double.NaN)));
    }
    if (!planStrategies.isEmpty()) {
      planMetrics.put("planStrategies", planStrategies);
    }

    Map<String, Object> metrics = new HashMap<>();
    metrics.put("count", people.size());
    metrics.put("healthMetrics", healthMetrics);
    metrics.put("planMetrics", planMetrics);

    return metrics;
  }
}
