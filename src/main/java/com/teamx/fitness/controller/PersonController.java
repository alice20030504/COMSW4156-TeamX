package com.teamx.fitness.controller;

import com.teamx.fitness.controller.dto.GoalPlanRequest;
import com.teamx.fitness.controller.dto.PersonCreateRequest;
import com.teamx.fitness.controller.dto.PersonCreatedResponse;
import com.teamx.fitness.controller.dto.PersonProfileResponse;
import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.AuthService;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Personal Controller - Endpoints for managing personal accounts and health metrics.
 *
 * <p>This controller provides endpoints for normal users (mobile clients) to manage their own
 * personal fitness data. Users authenticate with the generated client identifier.</p>
 */
@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
@Tag(
    name = "Personal Controller",
    description = "Endpoints for managing personal accounts and health metrics. "
        + "Only the authenticated user can view or modify their own data.")
public class PersonController {

  /** Service for handling person-related business logic. */
  @Autowired private PersonService personService;

  /** Repository for person data persistence. */
  @Autowired private PersonRepository personRepository;

  /** Service for handling authentication and authorization. */
  @Autowired private AuthService authService;

  /** BMI threshold for underweight classification. */
  private static final double BMI_UNDERWEIGHT = 18.5;

  /** BMI threshold for normal weight classification. */
  private static final double BMI_NORMAL = 25.0;

  /** BMI threshold for overweight classification. */
  private static final double BMI_OVERWEIGHT = 30.0;

  @PostMapping
  @Operation(
      summary = "Create a new person profile",
      description = "Registers a new person with name, weight, height, birth date, gender, and goal. "
          + "Returns the generated client identifier to use for subsequent requests."
          + " This is the only personal endpoint that does not require the `X-Client-ID` header.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Person created successfully",
          content = @Content(
              schema = @Schema(implementation = PersonCreatedResponse.class),
              examples = @ExampleObject(
                  value = """
                      {
                        "clientId": "mobile-3f2a4b1cd8e94bceb8c0b6a7dd5f1e92"
                      }
                      """))),
      @ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  public ResponseEntity<PersonCreatedResponse> createPerson(
      @Valid @RequestBody PersonCreateRequest request) {

    personService.calculateBMI(request.getWeight(), request.getHeight());

    if (!request.getBirthDate().isBefore(LocalDate.now())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "birthDate must be before today");
    }
    if (request.getGender() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "gender must be either MALE or FEMALE");
    }

    PersonSimple person = new PersonSimple();
    person.setName(request.getName().trim());
    person.setWeight(request.getWeight());
    person.setHeight(request.getHeight());
    person.setBirthDate(request.getBirthDate());
    person.setGender(request.getGender());
    person.setGoal(request.getGoal());
    person.setClientId(generateClientId());

    PersonSimple saved = personRepository.save(person);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new PersonCreatedResponse(saved.getClientId()));
  }

  @GetMapping("/me")
  @Operation(
      summary = "Get the current client profile",
      description = "Returns the persisted profile for the supplied X-Client-ID header.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Client identifier returned by POST /api/persons",
              example = "mobile-id1")
      })
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Profile retrieved successfully",
          content = @Content(schema = @Schema(implementation = PersonProfileResponse.class))),
      @ApiResponse(responseCode = "404", description = "Profile not found")
  })
  public ResponseEntity<PersonProfileResponse> getProfile() {
    PersonSimple person = requirePersonForClient(requireClientId());
    return ResponseEntity.ok(PersonProfileResponse.fromEntity(person));
  }

  @PostMapping("/plan")
  @Operation(
      summary = "Configure goal plan details",
      description = "Saves the target change, duration, and training frequency for the active goal.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Client identifier returned by POST /api/persons",
              example = "mobile-id1")
      })
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Plan stored successfully",
          content = @Content(schema = @Schema(implementation = PersonProfileResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid plan details")
  })
  public ResponseEntity<PersonProfileResponse> configureGoalPlan(
      @Valid @RequestBody GoalPlanRequest request) {
    PersonSimple person = requirePersonForClient(requireClientId());

    validatePlanRequest(person.getGoal(), request);

    person.setTargetChangeKg(request.getTargetChangeKg());
    person.setTargetDurationWeeks(request.getDurationWeeks());
    person.setTrainingFrequencyPerWeek(request.getTrainingFrequencyPerWeek());

    PersonSimple saved = personRepository.save(person);
    return ResponseEntity.ok(PersonProfileResponse.fromEntity(saved));
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update existing user information",
      description = "Allow editing of height, weight, and birth date. Requires authentication.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Client identifier returned by POST /api/persons",
              example = "mobile-id1")
      })
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "User updated successfully",
          content = @Content(schema = @Schema(implementation = PersonSimple.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<Object> updatePerson(
      @Parameter(description = "The person ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "Birth date for authentication (YYYY-MM-DD)", required = true)
      @RequestParam LocalDate birthDate,
      @Parameter(description = "Updated user data", required = true)
      @Valid @RequestBody PersonSimple updatedPerson) {

    if (!authService.validateUserAccess(id, birthDate)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("Invalid ID or birth date");
    }

    String clientId = requireClientId();
    Optional<PersonSimple> existing = personRepository.findByIdAndClientId(id, clientId);
    if (existing.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    updatedPerson.setId(id);
    updatedPerson.setClientId(clientId);
    personService.calculateBMI(updatedPerson.getWeight(), updatedPerson.getHeight());

    if (!updatedPerson.getBirthDate().isBefore(LocalDate.now())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "birthDate must be before today");
    }
    if (updatedPerson.getGender() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gender is required");
    }

    PersonSimple saved = personRepository.save(updatedPerson);
    return ResponseEntity.ok(saved);
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete person by ID",
      description = "Delete a person record if it belongs to the calling client.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Client identifier returned by POST /api/persons",
              example = "mobile-id1")
      })
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<Object> deletePerson(
      @Parameter(description = "The person ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "Birth date for authentication (YYYY-MM-DD)", required = true)
      @RequestParam LocalDate birthDate) {

    if (!authService.validateUserAccess(id, birthDate)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("Invalid ID or birth date");
    }

    String clientId = requireClientId();
    Optional<PersonSimple> existing = personRepository.findByIdAndClientId(id, clientId);
    if (existing.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    personRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(
      summary = "List persons for the client",
      description = "Return all person records belonging to the calling client.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Client identifier returned by POST /api/persons",
              example = "mobile-id1")
      })
  public ResponseEntity<List<PersonSimple>> listPersons() {
    String clientId = requireClientId();
    List<PersonSimple> persons = personRepository.findAllByClientId(clientId);
    return ResponseEntity.ok(persons);
  }

  @GetMapping("/health")
  @Operation(
      summary = "Service health check",
      description = "Return service status and metadata.")
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "UP");
    response.put("service", "Personal Fitness Management Service");
    response.put("version", "1.1.0");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/calories")
  @Operation(
      summary = "Calculate daily calorie needs based on parameters",
      description = "Calculate daily calorie requirements based on personal metrics including "
          + "weight, height, age, gender, and weekly training frequency. "
          + "Uses BMR (Basal Metabolic Rate) calculation with activity multiplier.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Client identifier returned by POST /api/persons",
              example = "mobile-id1")
      })
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Calorie calculation successful",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
                  {
                    "bmr": 1650.5,
                    "dailyCalories": 2475.75,
                    "weeklyTrainingFreq": 4
                  }
                  """))),
      @ApiResponse(responseCode = "400", description = "Invalid input parameters")
  })
  public ResponseEntity<Map<String, Object>> calculateDailyCalories() {
    PersonSimple person = requirePersonForClient(requireClientId());

    if (person.getTrainingFrequencyPerWeek() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Set a training frequency via /api/persons/plan before requesting calories");
    }

    Integer age = personService.calculateAge(person.getBirthDate());
    if (age == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "birthDate is required to compute calorie targets");
    }

    boolean isMale = Gender.MALE.equals(person.getGender());
    Double bmr = personService.calculateBMR(person.getWeight(), person.getHeight(), age, isMale);
    if (bmr == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Unable to compute BMR with the stored profile data");
    }
    Double dailyCalories = personService.calculateDailyCalorieNeeds(
        bmr, person.getTrainingFrequencyPerWeek());
    if (dailyCalories == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Unable to compute calorie needs with the stored plan");
    }

    Map<String, Object> response = new HashMap<>();
    response.put("goal", person.getGoal());
    response.put("targetChangeKg", person.getTargetChangeKg());
    response.put("targetDurationWeeks", person.getTargetDurationWeeks());
    response.put("trainingFrequencyPerWeek", person.getTrainingFrequencyPerWeek());
    response.put("bmr", bmr);
    response.put("dailyCalories", dailyCalories);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/bmi")
  @Operation(
      summary = "Get BMI using stored profile metrics",
      description = "Retrieves the BMI for the current client using the stored height and weight.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Client identifier returned by POST /api/persons",
              example = "mobile-id1")
      })
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "BMI retrieved successfully",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
                  {
                    "clientId": "mobile-3f2a4b1cd8e94bceb8c0b6a7dd5f1e92",
                    "weight": 75.5,
                    "height": 180.0,
                    "bmi": 23.3,
                    "category": "Normal weight"
                  }
                  """))),
      @ApiResponse(responseCode = "404", description = "Profile not found")
  })
  public ResponseEntity<Map<String, Object>> calculateBMI() {
    PersonSimple person = requirePersonForClient(requireClientId());
    Double bmi = personService.calculateBMI(person.getWeight(), person.getHeight());

    Map<String, Object> response = new HashMap<>();
    response.put("clientId", person.getClientId());
    response.put("weight", person.getWeight());
    response.put("height", person.getHeight());
    response.put("bmi", bmi);
    response.put("category", getBMICategory(bmi));

    return ResponseEntity.ok(response);
  }

  @GetMapping("/recommendation")
  @Operation(
      summary = "Provide a simple goal recommendation",
      description = "Returns a short motivational message based on the stored goal and plan.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Client identifier returned by POST /api/persons",
              example = "mobile-id1")
      })
  public ResponseEntity<Map<String, Object>> provideRecommendation() {
    PersonSimple person = requirePersonForClient(requireClientId());

    String message = buildRecommendation(person);

    Map<String, Object> response = new HashMap<>();
    response.put("goal", person.getGoal());
    response.put("message", message);
    response.put("targetChangeKg", person.getTargetChangeKg());
    response.put("targetDurationWeeks", person.getTargetDurationWeeks());
    return ResponseEntity.ok(response);
  }

  private void validatePlanRequest(FitnessGoal goal, GoalPlanRequest request) {
    if (goal == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "A goal must be selected before configuring a plan");
    }
    if (request.getTargetChangeKg() == null || request.getDurationWeeks() == null
        || request.getTrainingFrequencyPerWeek() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "All plan fields are required");
    }
  }

  private String buildRecommendation(PersonSimple person) {
    FitnessGoal goal = person.getGoal();
    Double change = person.getTargetChangeKg();
    Integer duration = person.getTargetDurationWeeks();

    if (goal == FitnessGoal.BULK) {
      if (change != null && duration != null) {
        return String.format(
            "Keep bulking: aim to gain %.1f kg over %d weeks. Stay consistent!",
            change,
            duration);
      }
      return "Keep bulking—focus on progressive overload and sufficient calories.";
    }

    if (goal == FitnessGoal.CUT) {
      if (change != null && duration != null) {
        return String.format(
            "Keep cutting: target %.1f kg over %d weeks. Stay on track!",
            change,
            duration);
      }
      return "Keep cutting—prioritize protein and maintain your calorie deficit.";
    }

    return "Stay consistent with your current plan.";
  }

  private String requireClientId() {
    String clientId = ClientContext.getClientId();
    if (clientId == null || clientId.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "X-Client-ID header is required");
    }
    return clientId;
  }

  private PersonSimple requirePersonForClient(String clientId) {
    return personRepository
        .findByClientId(clientId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No person profile found for supplied clientId"));
  }

  private String generateClientId() {
    final int maxAttempts = 1000;
    for (int suffix = 1; suffix <= maxAttempts; suffix++) {
      String candidate = ClientContext.MOBILE_PREFIX + "id" + suffix;
      if (personRepository.findByClientId(candidate).isEmpty()) {
        return candidate;
      }
    }
    throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR, "Unable to generate a unique client identifier");
  }

  private String getBMICategory(Double bmi) {
    if (bmi == null) {
      return "Unknown";
    }
    if (bmi < BMI_UNDERWEIGHT) {
      return "Underweight";
    } else if (bmi < BMI_NORMAL) {
      return "Normal weight";
    } else if (bmi < BMI_OVERWEIGHT) {
      return "Overweight";
    } else {
      return "Obese";
    }
  }
}
