package com.teamx.fitness.controller;

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
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

  /** Service for derived health metrics and recommendations. */
  @Autowired private HealthInsightService healthInsightService;

  /** BMI threshold for underweight classification. */
  private static final double BMI_UNDERWEIGHT = 18.5;

  /** BMI threshold for normal weight classification. */
  private static final double BMI_NORMAL = 25.0;

  /** BMI threshold for overweight classification. */
  private static final double BMI_OVERWEIGHT = 30.0;

  /** Rough calories required per kg of body weight change. */
  private static final double CALORIES_PER_KG = 7700.0;

  /** Number of days per week used when averaging calorie adjustments. */
  private static final double DAYS_PER_WEEK = 7.0;

  /** Default calorie adjustment when plan metrics are missing. */
  private static final double DEFAULT_PLAN_ADJUSTMENT = 300.0;

  /** Rounding step used for diet suggestions. */
  private static final double CALORIE_ROUNDING_STEP = 10.0;

  /** Maximum reasonable daily calorie deficit (kcal/day). */
  private static final double MAX_DAILY_CALORIE_DEFICIT = 1500.0;

  /** Maximum reasonable daily calorie surplus (kcal/day). */
  private static final double MAX_DAILY_CALORIE_SURPLUS = 1000.0;

  /** Minimum healthy weight in kg (for adults). */
  private static final double MIN_HEALTHY_WEIGHT_KG = 30.0;

  /** Maximum reasonable weight in kg. */
  private static final double MAX_REASONABLE_WEIGHT_KG = 200.0;

  /** Minimum healthy BMI. */
  private static final double MIN_HEALTHY_BMI = 15.0;

  /** Maximum reasonable BMI. */
  private static final double MAX_REASONABLE_BMI = 50.0;

  /** Default workouts per week when plan data is absent. */
  private static final int DEFAULT_WEEKLY_WORKOUTS = 4;

  /** Minimum workouts allowed per week. */
  private static final int MIN_WEEKLY_WORKOUTS = 1;

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
              examples = @ExampleObject("""
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
      description = "Saves the target weight, duration, and training frequency for the active goal.",
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
    validateTargetWeight(person, request.getTargetChangeKg());

    person.setTargetChangeKg(request.getTargetChangeKg());
    person.setTargetDurationWeeks(request.getDurationWeeks());
    person.setTrainingFrequencyPerWeek(request.getTrainingFrequencyPerWeek());
    person.setPlanStrategy(request.getPlanStrategy());

    PersonSimple saved = personRepository.save(person);
    return ResponseEntity.ok(PersonProfileResponse.fromEntity(saved));
  }

  @PutMapping("/me")
  @Operation(
      summary = "Update current profile information",
      description = "Allow editing of stored metrics using only the X-Client-ID header.",
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
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<PersonSimple> updatePerson(
      @Parameter(description = "Updated user data", required = true)
      @Valid @RequestBody PersonSimple updatedPerson) {

    PersonSimple existing = requirePersonForClient(requireClientId());

    personService.calculateBMI(updatedPerson.getWeight(), updatedPerson.getHeight());

    if (!updatedPerson.getBirthDate().isBefore(LocalDate.now())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "birthDate must be before today");
    }
    if (updatedPerson.getGender() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gender is required");
    }

    // Validate target weight if target change is being set
    if (updatedPerson.getTargetChangeKg() != null && updatedPerson.getWeight() != null) {
      validateTargetWeight(updatedPerson, updatedPerson.getTargetChangeKg());
    }

    String trimmedName = updatedPerson.getName() != null ? updatedPerson.getName().trim() : existing.getName();
    existing.setName(trimmedName);
    existing.setWeight(updatedPerson.getWeight());
    existing.setHeight(updatedPerson.getHeight());
    existing.setBirthDate(updatedPerson.getBirthDate());
    existing.setGender(updatedPerson.getGender());
    existing.setGoal(updatedPerson.getGoal());
    existing.setTargetChangeKg(updatedPerson.getTargetChangeKg());
    existing.setTargetDurationWeeks(updatedPerson.getTargetDurationWeeks());
    existing.setTrainingFrequencyPerWeek(updatedPerson.getTrainingFrequencyPerWeek());
    existing.setPlanStrategy(updatedPerson.getPlanStrategy());

    PersonSimple saved = personRepository.save(existing);
    return ResponseEntity.ok(saved);
  }

  @DeleteMapping("/me")
  @Operation(
      summary = "Delete current profile",
      description = "Delete the person record bound to the supplied X-Client-ID.",
      parameters = {
          @Parameter(
              name = "X-Client-ID",
              in = ParameterIn.HEADER,
              required = true,
              description = "Client identifier returned by POST /api/persons",
              example = "mobile-id1")
      })
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Deleted successfully"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<Map<String, String>> deletePerson() {
    String clientId = requireClientId();
    return personRepository
        .findByClientId(clientId)
        .map(
            existing -> {
              personRepository.delete(existing);
              Map<String, String> body = new HashMap<>();
              body.put("message", "Profile deleted successfully");
              body.put("clientId", clientId);
              return ResponseEntity.ok(body);
            })
        .orElseGet(
            () -> {
              Map<String, String> body = new HashMap<>();
              body.put("message", "No profile found for supplied client ID");
              body.put("clientId", clientId);
              return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
            });
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
              examples = @ExampleObject("""
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

    if (person.getPlanStrategy() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Select a planStrategy via /api/persons/plan");
    }

    if (person.getTargetChangeKg() == null || person.getTargetDurationWeeks() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "targetChangeKg and targetDurationWeeks are required");
    }
    if (person.getTargetDurationWeeks() <= 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "targetDurationWeeks must be greater than 0");
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
    Double dailyCalories =
        personService.calculateDailyCalorieNeeds(bmr, person.getTrainingFrequencyPerWeek());
    if (dailyCalories == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Unable to compute calorie needs with the stored plan");
    }

    Double targetWeight = person.getTargetChangeKg();
    if (targetWeight == null || person.getWeight() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "weight and targetChangeKg (target weight) are required");
    }

    double plannedDeltaKg = targetWeight - person.getWeight();
    double dailyAdjustmentCalories =
        Math.abs(plannedDeltaKg) * CALORIES_PER_KG
            / person.getTargetDurationWeeks()
            / DAYS_PER_WEEK;

    // Apply boundary checks for calorie adjustments
    boolean isCut = FitnessGoal.CUT.equals(person.getGoal());
    if (isCut && dailyAdjustmentCalories > MAX_DAILY_CALORIE_DEFICIT) {
      dailyAdjustmentCalories = MAX_DAILY_CALORIE_DEFICIT;
    } else if (!isCut && dailyAdjustmentCalories > MAX_DAILY_CALORIE_SURPLUS) {
      dailyAdjustmentCalories = MAX_DAILY_CALORIE_SURPLUS;
    }

    double recommendedCalories = isCut
        ? Math.max(0, dailyCalories - dailyAdjustmentCalories)
        : dailyCalories + dailyAdjustmentCalories;

    Map<String, Object> response = new HashMap<>();
    response.put("goal", person.getGoal());
    response.put("planStrategy", person.getPlanStrategy());
    response.put("targetChangeKg", person.getTargetChangeKg());
    response.put("targetDurationWeeks", person.getTargetDurationWeeks());
    response.put("trainingFrequencyPerWeek", person.getTrainingFrequencyPerWeek());
    response.put("bmr", bmr);
    response.put("maintenanceCalories", dailyCalories);
    response.put("calorieAdjustmentPerDay", isCut ? -dailyAdjustmentCalories : dailyAdjustmentCalories);
    response.put("recommendedDailyCalories", recommendedCalories);
    response.putAll(buildPlanDetails(person, null));

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
              examples = @ExampleObject("""
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

    // Validate that all goal plan fields are present
    if (person.getTargetChangeKg() == null
        || person.getTargetDurationWeeks() == null
        || person.getTrainingFrequencyPerWeek() == null
        || person.getPlanStrategy() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Cannot provide recommendation. All goal plan fields must be configured: "
              + "targetChangeKg, targetDurationWeeks, trainingFrequencyPerWeek, and planStrategy. "
              + "Please configure your goal plan first.");
    }

    HealthInsightResult insight = healthInsightService.buildInsights(person);

    Map<String, Object> response = new HashMap<>();
    response.put("goal", person.getGoal());
    response.put("message", insight.recommendation());
    response.put("bmi", insight.bmi());
    response.put("bmiCategory", insight.bmiCategory());
    response.put("healthIndex", insight.healthIndex());
    response.put("planAlignmentIndex", insight.planAlignmentIndex());
    response.put("overallScore", insight.overallScore());
    response.put("percentile", insight.percentile());
    if (insight.cohortWarning() != null) {
      response.put("cohortWarning", insight.cohortWarning());
    }
    // Add warning when plan alignment is 0
    if (insight.planAlignmentIndex() != null && insight.planAlignmentIndex() == 0.0) {
      response.put("planAlignmentWarning", 
          "Plan Alignment of 0 means your goal plan is unrealistic. "
          + "This could be due to: goal contradiction (e.g., BULK goal with weight loss target), "
          + "extremely aggressive weight change rates, unrealistic timeline, "
          + "insufficient training frequency, or mismatched plan strategy. "
          + "Please review and adjust your plan configuration to create a realistic plan.");
    }
    response.put("planStrategy", person.getPlanStrategy());
    response.put("targetChangeKg", person.getTargetChangeKg());
    response.put("targetDurationWeeks", person.getTargetDurationWeeks());
    response.put("trainingFrequencyPerWeek", person.getTrainingFrequencyPerWeek());
    response.putAll(buildPlanDetails(person, insight.planAlignmentIndex()));
    return ResponseEntity.ok(response);
  }

  private void validatePlanRequest(FitnessGoal goal, GoalPlanRequest request) {
    if (goal == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "A goal must be selected before configuring a plan");
    }
    if (request.getTargetChangeKg() == null || request.getDurationWeeks() == null
        || request.getTrainingFrequencyPerWeek() == null || request.getPlanStrategy() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "All plan fields are required");
    }
  }

  /**
   * Validates that the target weight is within reasonable bounds.
   *
   * <p>The {@code targetChangeKg} field stored on {@link PersonSimple} represents the <em>final
   * target weight</em> in kilograms (not the delta). This method derives the implied change from
   * the current weight and ensures the resulting target weight and BMI stay within safe ranges.
   *
   * @param person The person entity with current weight, height, and goal
   * @param targetWeightKg The desired final target weight in kg
   * @throws ResponseStatusException if target weight is unreasonable
   */
  private void validateTargetWeight(PersonSimple person, Double targetWeightKg) {
    if (person.getWeight() == null || targetWeightKg == null || person.getGoal() == null) {
      return; // Skip validation if required fields are missing
    }

    double currentWeight = person.getWeight();
    double targetWeight = targetWeightKg;
    double changeMagnitude = Math.abs(targetWeight - currentWeight);
    
    if (person.getGoal() == FitnessGoal.CUT) {
      // Check minimum weight
      if (targetWeight < MIN_HEALTHY_WEIGHT_KG) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(
                "Target weight (%.1f kg) is below the minimum healthy weight (%.1f kg). "
                    + "Losing %.1f kg from your current weight of %.1f kg would be unsafe. "
                    + "Please set a more realistic target weight.",
                targetWeight, MIN_HEALTHY_WEIGHT_KG, changeMagnitude, currentWeight));
      }
      
      // Check BMI if height is available
      if (person.getHeight() != null && person.getHeight() > 0) {
        double targetBmi = targetWeight / Math.pow(person.getHeight() / 100.0, 2);
        if (targetBmi < MIN_HEALTHY_BMI) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              String.format(
                  "Target BMI (%.1f) would be below the minimum healthy BMI (%.1f). "
                      + "Losing %.1f kg from your current weight of %.1f kg would result in an unsafe BMI. "
                      + "Please set a more realistic target weight.",
                  targetBmi, MIN_HEALTHY_BMI, changeMagnitude, currentWeight));
        }
      }
    } else if (person.getGoal() == FitnessGoal.BULK) {
      // Check maximum weight
      if (targetWeight > MAX_REASONABLE_WEIGHT_KG) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(
                "Target weight (%.1f kg) exceeds the maximum reasonable weight (%.1f kg). "
                    + "Gaining %.1f kg from your current weight of %.1f kg would be excessive. "
                    + "Please set a more realistic target weight.",
                targetWeight, MAX_REASONABLE_WEIGHT_KG, changeMagnitude, currentWeight));
      }
      
      // Check BMI if height is available
      if (person.getHeight() != null && person.getHeight() > 0) {
        double targetBmi = targetWeight / Math.pow(person.getHeight() / 100.0, 2);
        if (targetBmi > MAX_REASONABLE_BMI) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              String.format(
                  "Target BMI (%.1f) would exceed the maximum reasonable BMI (%.1f). "
                      + "Gaining %.1f kg from your current weight of %.1f kg would result in an unsafe BMI. "
                      + "Please set a more realistic target weight.",
                  targetBmi, MAX_REASONABLE_BMI, changeMagnitude, currentWeight));
        }
      }
    }
  }

  private Map<String, String> buildPlanDetails(PersonSimple person, Double planAlignmentIndex) {
    Map<String, String> details = new HashMap<>();
    PlanStrategy strategy = person.getPlanStrategy();
    if (strategy == null) {
      return details;
    }

    if (strategy == PlanStrategy.DIET || strategy == PlanStrategy.BOTH) {
      details.put("dietPlan", defaultDietPlan(person, planAlignmentIndex));
    }

    if (strategy == PlanStrategy.WORKOUT || strategy == PlanStrategy.BOTH) {
      details.put("workoutPlan", defaultWorkoutPlan(person, planAlignmentIndex));
    }

    return details;
  }

  private String defaultDietPlan(PersonSimple person, Double planAlignmentIndex) {
    // If plan alignment is 0, provide a warning message instead of specific diet plan
    if (planAlignmentIndex != null && planAlignmentIndex == 0.0) {
      return "Cannot provide a diet plan because your goal plan is unrealistic (Plan Alignment = 0). "
          + "Please review and adjust your plan configuration (target change, duration, training frequency, strategy) "
          + "to create a realistic plan before generating a diet plan.";
    }

    FitnessGoal goal = person.getGoal();
    double adjustment = DEFAULT_PLAN_ADJUSTMENT;
    Double targetWeight = person.getTargetChangeKg();
    Double currentWeight = person.getWeight();
    Integer durationWeeks = person.getTargetDurationWeeks();
    if (targetWeight != null && currentWeight != null && durationWeeks != null && durationWeeks > 0) {
      double deltaKg = targetWeight - currentWeight;
      adjustment =
          Math.abs(deltaKg * CALORIES_PER_KG / durationWeeks / DAYS_PER_WEEK);
    }
    
    // Apply boundary checks for calorie adjustments
    if (goal == FitnessGoal.CUT && adjustment > MAX_DAILY_CALORIE_DEFICIT) {
      adjustment = MAX_DAILY_CALORIE_DEFICIT;
    } else if (goal == FitnessGoal.BULK && adjustment > MAX_DAILY_CALORIE_SURPLUS) {
      adjustment = MAX_DAILY_CALORIE_SURPLUS;
    }
    
    adjustment = Math.round(adjustment / CALORIE_ROUNDING_STEP) * CALORIE_ROUNDING_STEP;

    if (goal == null) {
      return "Maintain a balanced meal plan with lean protein, whole grains, and plenty of vegetables.";
    }

    if (goal == FitnessGoal.CUT) {
      // Check if the adjustment was capped
      double uncappedAdjustment =
          targetWeight != null && currentWeight != null && durationWeeks != null && durationWeeks > 0
              ? Math.abs((targetWeight - currentWeight) * CALORIES_PER_KG / durationWeeks / DAYS_PER_WEEK)
              : DEFAULT_PLAN_ADJUSTMENT;
      
      if (uncappedAdjustment > MAX_DAILY_CALORIE_DEFICIT) {
        return String.format(
            "Aim for about %.0f kcal deficit per day (capped at maximum safe deficit) "
                + "with high-protein, veggie-rich meals and adequate hydration. "
                + "Your original plan would require %.0f kcal/day deficit, which is unsafe. "
                + "Consider extending your duration or reducing your target change.",
            adjustment, Math.round(uncappedAdjustment / CALORIE_ROUNDING_STEP) * CALORIE_ROUNDING_STEP);
      }
      return String.format(
          "Aim for about %.0f kcal deficit per day with high-protein, veggie-rich meals and adequate hydration.",
          adjustment);
    }
    
    // Check if surplus was capped
    double uncappedSurplus =
        targetWeight != null && currentWeight != null && durationWeeks != null && durationWeeks > 0
            ? Math.abs((targetWeight - currentWeight) * CALORIES_PER_KG / durationWeeks / DAYS_PER_WEEK)
            : DEFAULT_PLAN_ADJUSTMENT;
    
    if (uncappedSurplus > MAX_DAILY_CALORIE_SURPLUS) {
      return String.format(
          "Target roughly %.0f kcal surplus daily (capped at maximum safe surplus) "
              + "using lean proteins, complex carbs, and healthy fats spread across meals. "
              + "Your original plan would require %.0f kcal/day surplus, which may lead to excessive fat gain. "
              + "Consider extending your duration or reducing your target change.",
          adjustment, Math.round(uncappedSurplus / CALORIE_ROUNDING_STEP) * CALORIE_ROUNDING_STEP);
    }
    
    return String.format(
        "Target roughly %.0f kcal surplus daily using lean proteins, complex carbs, "
            + "and healthy fats spread across meals.",
        adjustment);
  }

  private String defaultWorkoutPlan(PersonSimple person, Double planAlignmentIndex) {
    // If plan alignment is 0, provide a warning message instead of specific workout plan
    if (planAlignmentIndex != null && planAlignmentIndex == 0.0) {
      return "Cannot provide a workout plan because your goal plan is unrealistic (Plan Alignment = 0). "
          + "Please review and adjust your plan configuration (target change, duration, training frequency, strategy) "
          + "to create a realistic plan before generating a workout plan.";
    }

    int frequency = person.getTrainingFrequencyPerWeek() != null
        ? person.getTrainingFrequencyPerWeek()
        : DEFAULT_WEEKLY_WORKOUTS;
    if (frequency < MIN_WEEKLY_WORKOUTS) {
      frequency = MIN_WEEKLY_WORKOUTS;
    }
    FitnessGoal goal = person.getGoal();
    if (goal == null) {
      return String.format(
          "Schedule %d total-body sessions each week combining strength, mobility, and light cardio.",
          frequency);
    }

    if (goal == FitnessGoal.CUT) {
      return String.format(
          "Schedule %d weekly sessions mixing strength and cardio (e.g., 3 strength, %d cardio) to support fat loss.",
          frequency,
          Math.max(1, frequency / 2));
    }
    return String.format(
        "Plan %d strength-focused sessions emphasising progressive overload, plus mobility work for recovery.",
        frequency);
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
